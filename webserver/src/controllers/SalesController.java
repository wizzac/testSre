package controllers;

import java.util.*;
import java.util.concurrent.*;

import org.json.JSONArray;
import org.json.JSONObject;
import utils.HttpClient;
import java.util.Map;

public class SalesController extends MainController {

    private int backendServerPort = 8888;
    private Double conversionRate = CurrencyController.currency;
    private Respuesta respuesta;
    private ExecutorService pool=Executors.newCachedThreadPool();

    //    private eventListener mListener;
    //    public void registrarEvento(eventListener mListener) {
    //        this.mListener = mListener;
    //    }


    public void index() {


        Respuesta respuesta=null;
        System.out.println("------------------Controler----------------");
        Map requestProperties = new LinkedHashMap();
        final String curr = "/currency_conversions?from=USD&to=ARS";
        requestProperties.put("baseUrl", "http://localhost:" + backendServerPort);
        requestProperties.put("method", "GET");
        requestProperties.put("body", new LinkedHashMap());
        requestProperties.put("headers", new LinkedHashMap());
        requestProperties.put("socketTimeout", 2000);
        requestProperties.put("connectionTimeout", 400);
        requestProperties.put("uriWithQueryString", curr);
        Integer userId = 0;
        try {
            JSONObject parameters = request.getJSONObject("parameters");
            JSONArray userIdArray = parameters.getJSONArray("userId");
            userId = Integer.valueOf(userIdArray.get(0).toString());
        } catch (Exception e) {
            setServiceFailedResponse("No id Provided");
        }
        if (respuesta == null) {
            EjecutarTarea tarea = new EjecutarTarea(userId, conversionRate, requestProperties);
            MyRunnable worker=new MyRunnable(tarea);
            try {
                Future<Respuesta> fut=pool.submit(worker);
                setRespuesta(fut.get());
            } catch (Exception e) {
                System.out.println(e.getMessage());
                setServiceFailedResponse("Too much time ");
            }
        }else {
            setServiceFailedResponse("No id Provided");
        }
        showResponse();
    }

    private void setServiceFailedResponse(String message) {
        this.respuesta = new Respuesta("{'message':'" + message + "'}", 200);
        return;
    }

    private void setRespuesta(Respuesta r) {
        this.respuesta = r;
        return;
    }

    private void showResponse(){
        setResponseBody(respuesta.getMsj());
        setResponseStatus(respuesta.getStatus());
        setResponseHeader("content-type", "application/json");
    }

    class MyRunnable implements Callable {

        private EjecutarTarea tarea;
        MyRunnable(EjecutarTarea tarea) {
            this.tarea=tarea;
        }

        @Override
        public Respuesta call() {
            Respuesta res=new Respuesta();
            try {
                Future<Respuesta> respuestaFuture = tarea.getCall();
                res=respuestaFuture.get();
            }catch (Exception e){
                System.out.println(e.getMessage());

            }
            return res;
        }
    }
}