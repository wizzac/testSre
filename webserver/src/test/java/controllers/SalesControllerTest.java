package controllers;

import org.junit.Test;
import utils.HttpClient;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

import static org.junit.Assert.*;

public class SalesControllerTest {

    private final SalesController controller=new SalesController();


    @Test
    public  void indexTestRequest() throws Exception{

        Random r= new Random();
        int low = 10;
        int high = 100;
        int result = r.nextInt(high-low) + low;
        Map response;
        Map requestProperties = new LinkedHashMap();
        requestProperties.put("baseUrl", "http://localhost:" + 8889);
        requestProperties.put("method", "GET");
        requestProperties.put("body", new LinkedHashMap());
        requestProperties.put("headers", new LinkedHashMap());
        requestProperties.put("socketTimeout", 3000);
        requestProperties.put("connectionTimeout", 2000);
        requestProperties.put("uriWithQueryString", "/sales?userId=" + result);
        response = HttpClient.executeRequest(requestProperties);
        Integer responseStatus = (Integer) response.get("status");
        assertTrue(responseStatus==200);
    }



}