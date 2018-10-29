package controllers;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.json.JSONArray;
import org.json.JSONObject;
import utils.HttpClient;
import java.util.Map;

public class SalesController extends MainController{

    private int backendServerPort = 8888;
    private Double conversionRate=CurrencyController.currency;
    private ExecutorService servicio = Executors.newFixedThreadPool(300);
    private Respuesta respuesta = new Respuesta();

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
        requestProperties.put("connectionTimeout", 5000);
        requestProperties.put("uriWithQueryString", curr);
        Integer responseStatus;


        CompletableFuture future=new CompletableFuture<>();


        try {
            for (int attempt = 0; attempt < 3; attempt++) {
                JSONObject parameters = request.getJSONObject("parameters");
                JSONArray userIdArray = parameters.getJSONArray("userId");
                Integer userId = Integer.valueOf(userIdArray.get(0).toString());
                finalResponse.put("sellerId", userId);

                requestProperties.put("uriWithQueryString", "/users/" + userId);
                response = HttpClient.executeRequest(requestProperties);
                responseStatus = (Integer) response.get("status");
                System.out.println("estado error " + responseStatus);
                if (responseStatus > 201) {
                    setServiceFailedResponse("could not get user information user");
                    continue;
                }
                Map responseBody = (Map) response.get("body");
                if (!"seller".equals(responseBody.get("user_type"))) {
                    setServiceFailedResponse("user is not seller");
                    break;
                } else {
                    EjecutarTarea tarea = new EjecutarTarea(backendServerPort, request, userId, conversionRate);
                    future = CompletableFuture.supplyAsync(() -> {
                        Future<Respuesta> promesa;
                        Respuesta tareaReal = new Respuesta();
                        try {
                            promesa = servicio.submit(tarea);
                            setRespuesta(promesa.get());
                        } catch (Exception e) {
                            setServiceFailedResponse("could not get user information");
                        }

                        //                    },servicio).thenApply(tareaReal->{
//                        System  ut.println("then apply ");
//
                        return 1;
//                    });
//                    break;
                    });
                }
            }
            future.get();
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
        setResponseBody(respuesta.getMsj());
        setResponseStatus(respuesta.getStatus());
        setResponseHeader("content-type", "application/json");
    }

    private void setServiceFailedResponse(String message){
        this.respuesta=new Respuesta("{'message':'" + message + "'}",200);
        return;
    }

    private void setRespuesta(Respuesta r){
        this.respuesta=r;
        return;
    }


}
