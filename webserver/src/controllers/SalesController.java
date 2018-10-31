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

public class SalesController extends MainController {

    private int backendServerPort = 8888;
    private Double conversionRate = CurrencyController.currency;
    private Respuesta respuesta;
//    private eventListener mListener;

//    public void registrarEvento(eventListener mListener) {
//        this.mListener = mListener;
//    }


    public void index() {

        JSONObject finalResponse = new JSONObject();
        Double totalAmount = 0D;
        Map requestProperties = new LinkedHashMap();
        final String curr = "/currency_conversions?from=USD&to=ARS";
        requestProperties.put("baseUrl", "http://localhost:" + backendServerPort);
        requestProperties.put("method", "GET");
        requestProperties.put("body", new LinkedHashMap());
        requestProperties.put("headers", new LinkedHashMap());
        requestProperties.put("socketTimeout", 2000);
        requestProperties.put("connectionTimeout", 300);
        requestProperties.put("uriWithQueryString", curr);
        Integer responseStatus;
        Future<Respuesta> respuestaFuture=null;
        CompletableFuture future = new CompletableFuture<>();
        Integer userId = 0;
        try {

            JSONObject parameters = request.getJSONObject("parameters");
            JSONArray userIdArray = parameters.getJSONArray("userId");
            userId = Integer.valueOf(userIdArray.get(0).toString());

        } catch (Exception e) {
            setServiceFailedResponse("No id Provided");
        }
        if(this.respuesta==null){
            EjecutarTarea tarea = new EjecutarTarea(backendServerPort, request, userId, conversionRate,requestProperties);
            respuestaFuture=tarea.getCall();

//            future = CompletableFuture.runAsync(() -> {
//                Future<Respuesta> promesa;
//                try {
//                    promesa = servicio.submit(tarea);
//                    setRespuesta(promesa.get());
//                } catch (Exception e) {
//                    setServiceFailedResponse("could not get user information");
//                }
//            });
//
//            try {
//                future.get();
//            } catch (Exception e) {
//                System.out.println(e.getMessage());
        }
//        }
        try {
            setRespuesta(respuestaFuture.get());
        } catch (Exception e) {
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

//
//    interface eventListener {
//        void reponse();
//    }
//
//    public void reponse() {
//        setResponseBody(respuesta.getMsj());
//        setResponseStatus(respuesta.getStatus());
//        setResponseHeader("content-type", "application/json");
//    }
//
//
//    class Responsor implements eventListener {
//        @Override
//        public void reponse() {
//            setResponseBody(respuesta.getMsj());
//            setResponseStatus(respuesta.getStatus());
//            setResponseHeader("content-type", "application/json");
//        }
//    }
}
