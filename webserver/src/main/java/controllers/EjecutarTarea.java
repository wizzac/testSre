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
public class EjecutarTarea extends MainController {
    private static Map response;
    private static int user;
    private static Respuesta r;
    private static Double conversionRate;
    private static Double total=0d;
    private static ExecutorService pool = Executors.newFixedThreadPool(10);
    private static Map requestProperties;

    public EjecutarTarea(int user,Double conversionRate,Map requestProperties){
        this.user =user;
        this.conversionRate =conversionRate;
        this.requestProperties =requestProperties;
    }
    //        @Override
    public static Respuesta call() {
//        public Respuesta call() {
        JSONObject finalResponse = new JSONObject();
        final String sold="/soldItems/";
        final String item="/items/";
        Integer responseStatus=201;
        Map headers=new LinkedHashMap();
        String content;
        CompletableFuture future=new CompletableFuture<>();
        CompletableFuture futureNotify=new CompletableFuture<>();
        try {
            do {
//                for (int attempt = 0; attempt < 3; attempt++) {
                finalResponse.put("sellerId", user);
                requestProperties.put("uriWithQueryString", "/users/" + user);

                response = HttpClient.executeRequest(requestProperties);
                responseStatus = (Integer) response.get("status");
                headers = (LinkedHashMap) response.get("headers");
                content=(String) headers.get("CONTENT-LENGTH");
                if (responseStatus > 201) {
                    setServiceFailedResponse("could not get user information user");
//                        continue;
                }
                if(!(content.equals("139"))){
                    setServiceFailedResponse("Incomplete message");
                    System.out.println("Mensaje con content lenght distinto al esperado");
                }
//                    else {
//                        break;
//                    }
//                }
            }while (responseStatus!=200 && !(content.equals("139")));

            Map responseBody = (Map) response.get("body");
            if (!"seller".equals(responseBody.get("user_type"))) {
                setServiceFailedResponse("user is not seller");
                return r;
            } else {
                requestProperties.put("uriWithQueryString",sold + user);
//                for (int attempt2 = 0; attempt2 < 3; attempt2++) {
                do{
                    response = HttpClient.executeRequest(requestProperties);
                    responseStatus = (Integer) response.get("status");
                    headers = (LinkedHashMap) response.get("headers");
                    content=(String) headers.get("CONTENT-LENGTH");
                    if (responseStatus > 201) {
                        setServiceFailedResponse("could not get soldItems for user " + user);
//                        continue;
                    }
                    if(!content.equals("75")){
                        setServiceFailedResponse("Incomplete message");
                        System.out.println("Mensaje con content lenght distinto al esperado");
                    }
//                    else{
//                        break;
//                    }
                }while (responseStatus!=200 && !content.equals("75"));
//                }
                List soldItems = (List) response.get("body");
                for (int i = 0; i < soldItems.size(); i++) {
                    Map itemJson = (Map) soldItems.get(i);
                    Long itemId = (Long) itemJson.get("id");
                    requestProperties.put("uriWithQueryString",item + itemId.toString());
                    future=CompletableFuture.runAsync(()->{
                        Double resParcial=0d;
                        Integer status=201;
                        String contentProducto;
                        do{
//                        for (int attempt3 = 0; attempt3 < 3; attempt3++) {
                            response = HttpClient.executeRequest(requestProperties);
                            status = (Integer) response.get("status");
                            Map headersProducto = (LinkedHashMap) response.get("headers");
                            contentProducto=(String) headersProducto.get("CONTENT-LENGTH");
                            if (status > 201 && !contentProducto.equals("51")) {
                                continue;
                            }else{
                                Map itemInfo = (Map) response.get("body");
                                resParcial=(Double) itemInfo.get("price");
                                totalPorTread(resParcial);
                                break;
                            }
//                        }
                        }while (status>201 && !contentProducto.equals("51"));
                    });
                }
            }

            future.get();
            Double totalAmountUSD = total / conversionRate;
            futureNotify=CompletableFuture.runAsync(()-> {
                notifyResponse(requestProperties, user, totalAmountUSD);
            });
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
        Integer responseStatus=201;

        String content;
        try{
//            for (int attempt = 0; attempt < 3; attempt++) {
            do {
                Map notoificationResponse = HttpClient.executeRequest(requestProperties);
                responseStatus= (Integer) notoificationResponse.get("status");
                Map header = (LinkedHashMap) notoificationResponse.get("headers");
                content=(String) header.get("CONTENT-LENGTH");
                if (responseStatus > 201) {
                    setServiceFailedResponse("could not notify result");
                    continue;
                }
                if(!(content.equals("36"))){
                    setServiceFailedResponse("could not notify result");
                    System.out.println("Mensaje con content lenght distinto al esperado");
                }
//                else {
//                    break;
//                }
//            }
            }while (responseStatus>201 &&!(content.equals("36")));
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

    public Future<Respuesta> getCall(){
        return pool.submit(()->{
            return call();
        });

    }


}
