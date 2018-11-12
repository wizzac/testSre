package controllers;
import org.json.JSONArray;
import org.json.JSONObject;
import utils.HttpClient;


import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;


//public class EjecutarTarea implements Callable {
public class EjecutarTarea extends MainController {
    private static Map response;
    private static int user;
    private static Respuesta r;
    private static Double conversionRate;
    private static Double total=0d;
    private static ForkJoinPool pool =new ForkJoinPool();
    private static Map requestProperties;
    private MainController mc;

    public EjecutarTarea(int user,Double conversionRate,Map requestProperties,MainController mc){
        this.user =user;
        this.conversionRate =conversionRate;
        this.requestProperties =requestProperties;
        this.mc=mc;
    }
    //        @Override
    public void call() {
//        public Respuesta call() {
        JSONObject finalResponse = new JSONObject();
        final String sold="/soldItems/";
        final String item="/items/";
        Integer responseStatus=201;
        Map headers;
        String content;
        Double futures=0d;
        try {
            do {
                finalResponse.put("sellerId", user);
                requestProperties.put("uriWithQueryString", "/users/" + user);
                response = HttpClient.executeRequest(requestProperties);
                responseStatus = (Integer) response.get("status");
                if (responseStatus > 201) {
                    setServiceFailedResponse("could not get user information user");
                }
            }while (responseStatus!=200);

            Map responseBody = (Map) response.get("body");
            if (!"seller".equals(responseBody.get("user_type"))) {
                setServiceFailedResponse("user is not seller");
            } else {
                requestProperties.put("uriWithQueryString", sold + user);
                do {
                    response = HttpClient.executeRequest(requestProperties);
                    responseStatus = (Integer) response.get("status");
                    if (responseStatus > 201) {
                        setServiceFailedResponse("could not get soldItems for user " + user);
                    }
                } while (responseStatus != 200 );
                List soldItems = (List) response.get("body");

                futures = soldItems
                        .stream()
                        .map(line -> {
                            Map itemJson = (Map) line;
                            Long itemId = (Long) itemJson.get("id");
                            requestProperties.put("uriWithQueryString", item + itemId.toString());
                            Double resParcial = 0d;
                            Integer status = 201;
                            do {
                                response = HttpClient.executeRequest(requestProperties);
                                status = (Integer) response.get("status");
                                try {
                                    Map itemInfo = (Map) response.get("body");
                                    resParcial = (Double) itemInfo.get("price");
                                } catch (Exception e) {
                                    continue;
                                }
                            } while (status > 201);
                            totalPorTread(resParcial);
                            return resParcial;
                        }).mapToDouble(precio -> (Double) precio).sum();
            }
            Double totalAmountUSD = futures/ conversionRate;
            CompletableFuture.runAsync(()-> {
                notifyResponse(requestProperties, user, totalAmountUSD);
            });
            finalResponse.put("totalAmount", total);
            finalResponse.put("totalAmountUSD", totalAmountUSD);
            r=new Respuesta(finalResponse.toString(),200);
        } catch (Exception e) {
            System.out.println("Exception " + e);
        }
        showResponse(r);
        returnResponse();
    }

    private static void notifyResponse(Map requestProperties,Integer userToNotify, Double amountToNotify){
        Map bodyToNotify = new LinkedHashMap();
        bodyToNotify.put("id", userToNotify);
        bodyToNotify.put("amount", amountToNotify);
        requestProperties.put("uriWithQueryString", "/notifications");
        requestProperties.put("body", bodyToNotify);
        Integer responseStatus=201;
        try{
            do {
                Map notoificationResponse = HttpClient.executeRequest(requestProperties);
                responseStatus= (Integer) notoificationResponse.get("status");
                if (responseStatus > 201) {
                    setServiceFailedResponse("could not notify result");
                    continue;
                }
            }while (responseStatus>201);
        }
        catch(Exception e){
            setServiceFailedResponse("could not notify result");
        }
    }

    private static void setServiceFailedResponse(String message){
        JSONObject finalResp=new JSONObject();
        try{
            finalResp.put("message",message);
            r=new Respuesta(finalResp.toString(),500);
        }catch (Exception ex){
            System.out.println("No se pudo setear la respuesta Fallida");
        }
        return;
    }

    private static void totalPorTread(Double suma){
        synchronized (total) {
            total += suma;
        }
    }

//    public Future<Respuesta> getCall(){
//        return pool.submit(()->{
//            return call();
//        });
//    }

    private void showResponse(Respuesta respuesta){
        setResponseBody(respuesta.getMsj());
        setResponseStatus(respuesta.getStatus());
        setResponseHeader("content-type", "application/json");
    }

}
