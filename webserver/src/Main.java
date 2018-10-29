import controllers.CurrencyController;
import org.json.JSONObject;
import sockets.HttpSocket;
import sockets.HttpSocketManager;
import utils.HttpClient;

import java.util.LinkedHashMap;
import java.util.Map;


public class Main {

	public static void main(String[] args) {
		int serverPort = 8889;

		System.out.println("Starting socket");
		for(int i=0; i<args.length; i++){
			if(args[i].indexOf("=")>0){
				String key = args[i].split("=")[0];
				String value = args[i].split("=")[1];
				if("serverPort".equalsIgnoreCase(key)){
					serverPort = Integer.valueOf(value);
				}
			}
		}
		
		System.out.println("Listenning on port: " + serverPort + ". Please check it sending a request to http://localhost:" + serverPort + "/ping");
		CurrencyController p=new CurrencyController();
		p.initCurrency();
		HttpSocket serverSocket = new HttpSocket(serverPort);
		HttpSocketManager.init();
		System.out.println(CurrencyController.currency);
		HttpSocketManager.executor.execute(serverSocket);

	}



}
