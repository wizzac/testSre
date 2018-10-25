package controllers;

import java.io.BufferedReader;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONObject;
import utils.HttpClient;
public class SalesController extends MainController{
	private int backendServerPort = 8888;


	public void index() {
		
		JSONObject finalResponse = new JSONObject();
		Double totalAmount = 0D;
		Map requestProperties = new LinkedHashMap();
		requestProperties.put("baseUrl", "http://localhost:" + backendServerPort);
		requestProperties.put("method", "GET");
		requestProperties.put("body", new LinkedHashMap());
		requestProperties.put("headers", new LinkedHashMap());
		requestProperties.put("socketTimeout", 15000);
		requestProperties.put("connectionTimeout", 3000);
		
		try{



			JSONObject parameters = request.getJSONObject("parameters");
			JSONArray userIdArray = parameters.getJSONArray("userId");
			Integer userId = Integer.valueOf(userIdArray.get(0).toString());
			finalResponse.put("sellerId", userId);



			//-------------------------------------------------------------------
			//Request para obtener el usuario y chequear si es de tipo 'seller'
			requestProperties.put("uriWithQueryString", "/users/" + userId);
			Map response = HttpClient.executeRequest(requestProperties);
			Integer responseStatus = (Integer) response.get("status");
			if(responseStatus>201){
				setServiceFailedResponse("could not get user information");
				return;
			}

			Map responseBody = (Map) response.get("body");


			//-------------------------------------------------------------------
			//Request para obtener los items vendidos por ese seller
			requestProperties.put("uriWithQueryString", "/soldItems/" + userId.toString());
			response = HttpClient.executeRequest(requestProperties);
			responseStatus = (Integer) response.get("status");
			if(responseStatus>201){
				setServiceFailedResponse("could not get soldItems for user " + userId);
				return;
			}
			List soldItems = (List) response.get("body");
			
			//-------------------------------------------------------------------
			//Iterar para obtener el precio de cada uno de esos items vendidos
			for(int i=0; i<soldItems.size(); i++){
				Map itemJson = (Map) soldItems.get(i);
				Long itemId = (Long) itemJson.get("id");
				
				//Request para obtener cada item y así saber el precio, y sumarlo
				requestProperties.put("uriWithQueryString", "/items/" + itemId.toString());
				response = HttpClient.executeRequest(requestProperties);
				responseStatus = (Integer) response.get("status");
				if(responseStatus>201){
					setServiceFailedResponse("could not get item information");
					return;
				}
				Map itemInfo = (Map) response.get("body");
				totalAmount += (Double) itemInfo.get("price");
			}
			
			finalResponse.put("totalAmount", totalAmount);
			
			//--------------------------------------------------------------------
			//Obtención de tasa de cambio a dolares
			requestProperties.put("uriWithQueryString", "/currency_conversions?from=USD&to=ARS");
			response = HttpClient.executeRequest(requestProperties);
			responseStatus = (Integer) response.get("status");
			if(responseStatus>201){
				setServiceFailedResponse("could not get currency information");
				return;
			}
			Map currencyInfo = (Map) response.get("body");
			Double conversionRate = (Double) currencyInfo.get("rate");
			Double totalAmountUSD = totalAmount/conversionRate;
			finalResponse.put("totalAmountUSD", totalAmountUSD);
			
			//--------------------------------------------------------------------
			//Validación: si el usuario no es de tipo seller, entonces devuelve error, 
			//porque no es necesario devolver el monto de los items.
			if(!"seller".equals((String) responseBody.get("user_type"))){
				//Si no es seller no retorna 200
				setResponseBody("{'message':'user is not seller'}");
				setResponseStatus(500);
				setResponseHeader("content-type", "application/json");
				return;
			}
			
			//------------------------------------------------------------------- 
			//Notificación del monto total que se va a devolver
			notifyResponse(userId, totalAmountUSD);
			
			//-------------------------------------------------------------------
			//Seteo de respuesta
			setResponseBody(finalResponse);
			setResponseStatus(200);
			setResponseHeader("content-type", "application/json");
		}
		catch(Exception e){
			System.out.println("Exception " + e);
		}
		
	}
	
	private void notifyResponse(Integer userToNotify, Double amountToNotify){
		Map bodyToNotify = new LinkedHashMap();
		bodyToNotify.put("id", userToNotify);
		bodyToNotify.put("amount", amountToNotify);
		
		Map requestProperties = new LinkedHashMap();
		requestProperties.put("baseUrl", "http://localhost:" + backendServerPort);
		requestProperties.put("method", "POST");
		requestProperties.put("uriWithQueryString", "/notifications");
		requestProperties.put("body", bodyToNotify);
		requestProperties.put("headers", new LinkedHashMap());
		
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
		setResponseBody("{'message':'" + message + "'}");
		setResponseStatus(500);
		setResponseHeader("content-type", "application/json");
		return;
	}
}
