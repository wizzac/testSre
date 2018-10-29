package controllers;
import org.json.JSONObject;
import utils.HttpClient;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;


//public class EjecutarTarea implements Runnable {
public class EjecutarTarea implements Callable {
    private int backendServerPort;
    private JSONObject request;
    private Map response;
    private int user;
    private Respuesta r= new Respuesta();
    private Double conversionRate;
    private Double total=0d;



    public EjecutarTarea(Integer backendServerPort, JSONObject request,int user,Double conversionRate){
        this.backendServerPort=backendServerPort;
        this.request=request;
        this.user=user;
        this.conversionRate=conversionRate;
    }

    @Override
    public Respuesta call() {
//    public void run() {

        JSONObject finalResponse = new JSONObject();
        Double totalAmount = 0D;
        Map requestProperties = new LinkedHashMap();
        requestProperties.put("baseUrl", "http://localhost:" + backendServerPort);
        requestProperties.put("method", "GET");
        requestProperties.put("body", new LinkedHashMap());
        requestProperties.put("headers", new LinkedHashMap());
        requestProperties.put("socketTimeout", 1000);
        requestProperties.put("connectionTimeout", 5000);
        final String sold="/soldItems/";
        final String item="/items/";
        try {

            Integer userId = user;
            Integer responseStatus;

            requestProperties.put("uriWithQueryString",sold + userId.toString());
            for (int attempt = 0; attempt < 3; attempt++) {
                response = HttpClient.executeRequest(requestProperties);
                responseStatus = (Integer) response.get("status");
                if (responseStatus > 201) {
                    setServiceFailedResponse("could not get soldItems for user " + userId);
                    continue;
                }else{
                    break;
                }
            }
            List soldItems = (List) response.get("body");
            //Iterar para obtener el precio de cada uno de esos items vendidos
            CompletableFuture future=new CompletableFuture<>();
            Future<Double> promesa;

            for (int i = 0; i < soldItems.size(); i++) {
                Map itemJson = (Map) soldItems.get(i);
                Long itemId = (Long) itemJson.get("id");


                requestProperties.put("uriWithQueryString",item  + itemId.toString());
                //Request para obtener cada item y así saber el precio, y sumarlo
                System.out.println("INIT ITEM: " + i);
                future=CompletableFuture.runAsync(()->{

                    Double resParcial=0d;
                    for (int attempt = 0; attempt < 3; attempt++) {
//                        System.out.println("Llamada  "+requestProperties);
                        response = HttpClient.executeRequest(requestProperties);
                        Integer status = (Integer) response.get("status");
                        if (status > 201) {
//                            System.out.println("Error Estado: "+status+" "+response.toString());
                            continue;
                        }else{
//                            System.out.println("Success Estado: "+status+" "+response.toString());
                            Map itemInfo = (Map) response.get("body");
                            resParcial=(Double) itemInfo.get("price");
                            totalPorTread(resParcial);
                            break;
                        }
                    }
//                    return resParcial;
                });
//                        .thenApply(precio-> {
//                            totalPorTread(precio);
//                            return 1;
//                    }
//                );

//                Map itemInfo = (Map) response.get("body");
//                totalAmount += (Double) itemInfo.get("price");
                System.out.println("ITEM: " + i);

            }


            finalResponse.put("totalAmount", this.total);
            Double totalAmountUSD = this.total / conversionRate;
            finalResponse.put("totalAmountUSD", totalAmountUSD);
            notifyResponse(requestProperties,userId, totalAmountUSD);
            r=new Respuesta(finalResponse.toString(),200);

        } catch (Exception e) {
            System.out.println("Exception " + e);
        }
        return r;
    }

    private void notifyResponse(Map requestProperties,Integer userToNotify, Double amountToNotify){
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
            return;
        }
    }

    private void setServiceFailedResponse(String message){
        JSONObject finalResp=new JSONObject();
        try{
            finalResp.put("message",message);
            r=new Respuesta(finalResp.toString(),200);
        }catch (Exception ex){
//            System.out.println(ex.getMessage());
        }
//        System.out.println(r.getMsj());
        return;
    }


    private void totalPorTread(Double suma){
//        System.out.println("SUMO " + suma);
        synchronized (total) {
            total += suma;
//            System.out.println("AHORA SUMO " + total);
        }
    }

}
