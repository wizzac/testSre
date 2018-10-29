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
        requestProperties.put("socketTimeout", 5000);
        requestProperties.put("connectionTimeout", 1000);
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
            future.get();
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

        }
        return;
    }


    private void totalPorTread(Double suma){
        synchronized (total) {
            total += suma;
        }
    }

}
