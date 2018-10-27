package controllers;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.json.JSONArray;
import org.json.JSONObject;
import utils.HttpClient;
import java.util.Map;
public class SalesController extends MainController{

    private int backendServerPort = 8888;
    //	private ThreadPool pool=new ThreadPool(300);
    private ExecutorService servicio = Executors.newFixedThreadPool(300);


    public void index() {

        JSONObject finalResponse = new JSONObject();
        Double totalAmount = 0D;
        Map requestProperties = new LinkedHashMap();
        final String curr="/currency_conversions?from=USD&to=ARS";
        requestProperties.put("baseUrl", "http://localhost:" + backendServerPort);
        requestProperties.put("method", "GET");
        requestProperties.put("body", new LinkedHashMap());
        requestProperties.put("headers", new LinkedHashMap());
        requestProperties.put("socketTimeout", 1000);
        requestProperties.put("connectionTimeout", 1000);
        Respuesta respuesta = new Respuesta();
        try {

            JSONObject parameters = request.getJSONObject("parameters");
            JSONArray userIdArray = parameters.getJSONArray("userId");
            Integer userId = Integer.valueOf(userIdArray.get(0).toString());
            finalResponse.put("sellerId", userId);

            requestProperties.put("uriWithQueryString", curr);
            response = HttpClient.executeRequest(requestProperties);
            Integer responseStatus = (Integer) response.get("status");
            if (responseStatus > 201) {
                setServiceFailedResponse("could not get currency information", respuesta);
            } else {
                Map currencyInfo = (Map) response.get("body");
                Double conversionRate = (Double) currencyInfo.get("rate");

                requestProperties.put("uriWithQueryString", "/users/" + userId);
                response = HttpClient.executeRequest(requestProperties);
                responseStatus = (Integer) response.get("status");
                if (responseStatus > 201) {
                    setServiceFailedResponse("could not get user information", respuesta);
                    return;
                }

                Map responseBody = (Map) response.get("body");
                if (!"seller".equals(responseBody.get("user_type"))) {
                    setServiceFailedResponse("user is not seller", respuesta);
                } else {
                    EjecutarTarea tarea = new EjecutarTarea(backendServerPort, request, userId,conversionRate);
                    servicio.submit(tarea);
                    Future<Respuesta> promesa = servicio.submit(tarea);
                    respuesta = promesa.get();
                }

        }
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
        setResponseBody(respuesta.getMsj());
        setResponseStatus(respuesta.getStatus());
        setResponseHeader("content-type", "application/json");

    }

    private void setServiceFailedResponse(String message,Respuesta respuesta){

        respuesta=new Respuesta("{'message':'" + message + "'}",200);

        return;
    }



}
