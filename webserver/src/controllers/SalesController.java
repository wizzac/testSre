package controllers;

import java.io.BufferedReader;
import java.util.*;
import java.util.concurrent.ExecutorService;

import org.json.JSONArray;
import org.json.JSONObject;
import utils.HttpClient;

public class SalesController extends MainController{

	private int backendServerPort = 8888;
	private ThreadPool pool=new ThreadPool(300);


	public void index() {
		EjecutarTarea main=new EjecutarTarea(backendServerPort,request);
		pool.execute(main);

	}

	private void setServiceFailedResponse(String message){
		setResponseBody("{'message':'" + message + "'}");
		setResponseStatus(500);
		setResponseHeader("content-type", "application/json");
		return;
	}

}
