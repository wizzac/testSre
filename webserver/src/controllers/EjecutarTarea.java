package controllers;
import org.json.JSONObject;
import utils.HttpClient;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;


//public class EjecutarTarea implements Runnable {
public class EjecutarTarea implements Callable {
    private int backendServerPort;
    private JSONObject request;
    private Map response;
    private int user;
    private Respuesta r= new Respuesta();
    private Double conversionRate;



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
        requestProperties.put("socketTimeout", 150);
        requestProperties.put("connectionTimeout", 1000);
        final String sold="/soldItems/";
        final String item="/items/";
        try {

            Integer userId = user;
            Integer responseStatus;

            requestProperties.put("uriWithQueryString",sold + userId.toString());
            response = HttpClient.executeRequest(requestProperties);
            responseStatus = (Integer) response.get("status");
            if (responseStatus > 201) {
                setServiceFailedResponse("could not get soldItems for user " + userId);
                return r;
            }
            List soldItems = (List) response.get("body");

            //Iterar para obtener el precio de cada uno de esos items vendidos

            for (int i = 0; i < soldItems.size(); i++) {
                Map itemJson = (Map) soldItems.get(i);
                Long itemId = (Long) itemJson.get("id");
                //Request para obtener cada item y así saber el precio, y sumarlo

                    requestProperties.put("uriWithQueryString",item  + itemId.toString());
                response = HttpClient.executeRequest(requestProperties);
                responseStatus = (Integer) response.get("status");
                if (responseStatus > 201) {
                    setServiceFailedResponse("could not get item information");
                    return r;
                }
                Map itemInfo = (Map) response.get("body");
                totalAmount += (Double) itemInfo.get("price");
            }
            finalResponse.put("totalAmount", totalAmount);

            Double totalAmountUSD = totalAmount / conversionRate;
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

        Map notoificationResponse = HttpClient.executeRequest(requestProperties);
        try{
            Integer responseStatus = (Integer) notoificationResponse.get("status");
            if(responseStatus>201){
                setServiceFailedResponse("could not notify result");
                return;
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
            System.out.println(ex.getMessage());
        }
        System.out.println(r.getMsj());
        return;
    }



}
