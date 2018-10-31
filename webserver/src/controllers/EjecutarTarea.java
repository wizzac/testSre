package controllers;
import org.json.JSONArray;
import org.json.JSONObject;
import utils.HttpClient;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;


//public class EjecutarTarea implements Callable {
public class EjecutarTarea {
    private static Map response;
    private static int user;
    private static Respuesta r;
    private static Double conversionRate;
    private static Double total=0d;
    private static ExecutorService pool = Executors.newFixedThreadPool(10);
    private static Map requestProperties;


    public EjecutarTarea(int user,Double conversionRate,Map requestProperties){
        this.user=user;
        this.r=r;
        this.conversionRate=conversionRate;
        this.requestProperties=requestProperties;
    }

    //    @Override
    public static Respuesta call() {
        JSONObject finalResponse = new JSONObject();
        final String sold="/soldItems/";
        final String item="/items/";
        Integer responseStatus;
        CompletableFuture future=new CompletableFuture<>();
        CompletableFuture futureNotify=new CompletableFuture<>();
        try {
            for (int attempt = 0; attempt < 3; attempt++) {
                finalResponse.put("sellerId", user);
                requestProperties.put("uriWithQueryString", "/users/" + user);
                response = HttpClient.executeRequest(requestProperties);
                responseStatus = (Integer) response.get("status");
                if (responseStatus > 201) {
                    setServiceFailedResponse("could not get user information user");
                    continue;
                }
            }
            Map responseBody = (Map) response.get("body");
            if (!"seller".equals(responseBody.get("user_type"))) {
                setServiceFailedResponse("user is not seller");
                return r;
            } else {
                requestProperties.put("uriWithQueryString",sold + user);
                for (int attempt2 = 0; attempt2 < 3; attempt2++) {
                    response = HttpClient.executeRequest(requestProperties);
                    responseStatus = (Integer) response.get("status");
                    if (responseStatus > 201) {
                        setServiceFailedResponse("could not get soldItems for user " + user);
                        continue;
                    }else{
                        break;
                    }
                }
                List soldItems = (List) response.get("body");
                for (int i = 0; i < soldItems.size(); i++) {
                    Map itemJson = (Map) soldItems.get(i);
                    Long itemId = (Long) itemJson.get("id");
                    requestProperties.put("uriWithQueryString",item + itemId.toString());
                    future=CompletableFuture.runAsync(()->{
                        Double resParcial=0d;
                        for (int attempt3 = 0; attempt3 < 3; attempt3++) {
                            response = HttpClient.executeRequest(requestProperties);
                            Integer status = (Integer) response.get("status");
                            if (status > 201) {
                                continue;
                            }else{
                                Map itemInfo = (Map) response.get("body");
                                resParcial=(Double) itemInfo.get("price");
                                totalPorTread(resParcial);
                                break;
                            }
                        }
                    });
                }
            }

            future.get();
            Double totalAmountUSD = total / conversionRate;
            futureNotify=CompletableFuture.runAsync(()-> {
                notifyResponse(requestProperties, user, totalAmountUSD);
            });
            futureNotify.get();
            finalResponse.put("totalAmount", total);
            finalResponse.put("totalAmountUSD", totalAmountUSD);
            r=new Respuesta(finalResponse.toString(),200);
        } catch (Exception e) {
            System.out.println("Exception " + e);
        }
        return r;
    }

    private static void notifyResponse(Map requestProperties,Integer userToNotify, Double amountToNotify){
        Map bodyToNotify = new LinkedHashMap();
        bodyToNotify.put("id", userToNotify);
        bodyToNotify.put("amount", amountToNotify);
        requestProperties.put("uriWithQueryString", "/notifications");
        requestProperties.put("body", bodyToNotify);
        try{
            for (int attempt = 0; attempt < 3; attempt++) {
                Map notoificationResponse = HttpClient.executeRequest(requestProperties);
                Integer responseStatus = (Integer) notoificationResponse.get("status");
                if (responseStatus > 201) {
                    setServiceFailedResponse("could not notify result");
                    continue;
                }else{
                    break;
                }
            }
        }
        catch(Exception e){
            setServiceFailedResponse("could not notify result");
        }
    }

    private static void setServiceFailedResponse(String message){
        JSONObject finalResp=new JSONObject();
        try{
            finalResp.put("message",message);
            r=new Respuesta(finalResp.toString(),200);
        }catch (Exception ex){

        }
        return;
    }

    private static void totalPorTread(Double suma){
        synchronized (total) {
            total += suma;
        }
    }

    public static Future<Respuesta> getCall(){
        return pool.submit(()->{
            return call();
        });
    }




}
