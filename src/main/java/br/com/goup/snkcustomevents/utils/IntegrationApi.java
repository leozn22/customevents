package br.com.goup.snkcustomevents.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class IntegrationApi {

	public static String send(String urlWs, String params, String method) throws Exception {
	    URL url;
	    HttpURLConnection connection = null;

	    try {
	        url        = new URL(urlWs);
	        connection = (HttpURLConnection) url.openConnection();
	        connection.setRequestMethod(method);
	        connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
	        connection.setRequestProperty("Content-Lenght", "" + Integer.toString(params.getBytes().length));
	        connection.setRequestProperty("Content-Language", "pt-BR");
	        connection.setUseCaches(false);
	        connection.setDoInput(true);
	        connection.setDoOutput(true);

	        // Envio
	        OutputStreamWriter outPutStream = new OutputStreamWriter(connection.getOutputStream(), "utf-8");
	        outPutStream.write(params);
	        outPutStream.flush();
	        outPutStream.close();

	        // Recepção
	        InputStream inputStream       = connection.getInputStream();
	        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));

	        String row;
	        StringBuffer result = new StringBuffer();

	        while((row = bufferedReader.readLine()) != null) {
	        	result.append(row);
	        	result.append('\r');
	        }

	        bufferedReader.close();

	        return result.toString();

	    } 
	    catch (Exception e) { 
	    	throw new Exception("API INTEGRATION FAILED: "+ e.getMessage());
	    } 
	    finally {
	        if(connection != null) {
	            connection.disconnect();
	        }
	    }
	}
}
