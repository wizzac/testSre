package controllers;

import java.util.*;
import java.util.concurrent.*;
import static org.mockito.Mockito.*;
import org.json.JSONArray;
import org.json.JSONObject;
import utils.HttpClient;
import java.util.Map;


public class SalesController extends MainController {

    private int backendServerPort = 8888;
    private Double conversionRate = controllers.CurrencyController.currency;
    private controllers.Respuesta respuesta;
    private ExecutorService pool=Executors.newCachedThreadPool();

    public void index() {
//        controllers.Respuesta respuesta=null;
        System.out.println("------------------Controler----------------");
        Map requestProperties = new LinkedHashMap();
        requestProperties.put("baseUrl", "http://localhost:" + backendServerPort);
        requestProperties.put("method", "GET");
        requestProperties.put("body", new LinkedHashMap());
        requestProperties.put("headers", new LinkedHashMap());
        requestProperties.put("socketTimeout", 2000);
        requestProperties.put("connectionTimeout", 400);
        Integer userId = 0;
        try {
            JSONObject parameters = request.getJSONObject("parameters");
            JSONArray userIdArray = parameters.getJSONArray("userId");
            userId = Integer.valueOf(userIdArray.get(0).toString());
        } catch (Exception e) {
            setServiceFailedResponse("No id Provided");
        }
        if (this.respuesta == null) {
            EjecutarTarea tarea = new EjecutarTarea(userId, conversionRate, requestProperties);
            MyCallable worker=new MyCallable(tarea);
            try {
                Future<Respuesta> fut=pool.submit(worker);
                setRespuesta(fut.get());
            } catch (Exception e) {
                setServiceFailedResponse("Ocurrio un error inesperado");
            }
        }else {
            setServiceFailedResponse("No id Provided");
        }
        showResponse();
    }

    private void setServiceFailedResponse(String message) {
        this.respuesta = new Respuesta("{'message':'" + message + "'}", 500);
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

}