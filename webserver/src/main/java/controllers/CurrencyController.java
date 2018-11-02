package controllers;

import utils.HttpClient;

import java.util.LinkedHashMap;
import java.util.Map;

public class CurrencyController {

    public static Double currency=0d;

    public  void initCurrency() {
        Map requestProperties = new LinkedHashMap();
        final String curr = "/currency_conversions?from=USD&to=ARS";
        requestProperties.put("baseUrl", "http://localhost:" + "8888");
        requestProperties.put("method", "GET");
        requestProperties.put("body", new LinkedHashMap());
        requestProperties.put("headers", new LinkedHashMap());
        requestProperties.put("socketTimeout", 1000);
        requestProperties.put("connectionTimeout", 1000);
        requestProperties.put("uriWithQueryString", curr);
        Integer responseStatus;
        Double conversionRate=0d;
        for (int attempt = 0; attempt < 3; attempt++) {
            try {
                Map response = HttpClient.executeRequest(requestProperties);
                responseStatus = (Integer) response.get("status");
                if (responseStatus > 201) {
                    continue;
                } else {
                    Map currencyInfo = (Map) response.get("body");
                    conversionRate = (Double) currencyInfo.get("rate");
                    break;
                }
            }catch (Exception e){
                System.out.println(e.getMessage());
            }
        }
        currency=conversionRate;
        //si esto es 0 deberia tirar un error en la aplicacion y no dejar seguir
        //supongamos que esto no va a pasar nunca. pero queda contemplado

    }
}
