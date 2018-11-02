package controllers;

import java.io.BufferedReader;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONObject;
import utils.HttpClient;
public class PingController extends MainController{
	 
	public void index() {
			setResponseBody("pong");
			setResponseStatus(200);
	}
}
