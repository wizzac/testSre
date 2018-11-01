package controllers;

import org.junit.Assert;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Future;

import static org.junit.Assert.*;

public class EjecutarTareaTest {

    public int user;
    public Double rate;
    public Map map;

    public void init(){
        Random r = new Random();
        int low = 10;
        int high = 100;
        user = r.nextInt(high-low) + low;
        rate= 40d;
        Map requestProperties = new LinkedHashMap();
        requestProperties.put("baseUrl", "http://localhost:" + 8888);
        requestProperties.put("method", "GET");
        requestProperties.put("body", new LinkedHashMap());
        requestProperties.put("headers", new LinkedHashMap());
        requestProperties.put("socketTimeout", 2000);
        requestProperties.put("connectionTimeout", 400);
        map=requestProperties;
    }

    @Test
    public void call() throws Exception {
        init();
        final EjecutarTarea controller=new EjecutarTarea(user,rate,map);
        Respuesta resp= controller.call();
        assertTrue(resp!=null);
        assertTrue(resp.getMsj()!=null);
        assertEquals(resp.getStatus(),200);
    }

    @Test
    public void getCall() throws Exception {
        init();
        final EjecutarTarea controller=new EjecutarTarea(user,rate,map);
        for(int i=0;i<3;i++) {
            Future<Respuesta> resp = controller.getCall();
            assertTrue(resp != null);
            Respuesta respuesta = resp.get();
            assertTrue(respuesta.getMsj() != null);
            assertEquals(respuesta.getStatus(), 200);
        }
    }

}