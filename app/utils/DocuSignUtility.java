package utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Date;
import java.util.Map;

import org.apache.commons.io.IOUtils;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartsheet.api.Smartsheet;
import com.smartsheet.api.SmartsheetBuilder;
import com.smartsheet.api.SmartsheetException;
import com.smartsheet.api.models.Attachment;

import play.Logger;
import play.libs.F.Promise;
import play.libs.F.Function;
import play.libs.Json;
import play.libs.WS;
import play.libs.WS.WSRequestHolder;
import models.Document;
import models.Envelope;
import play.libs.WS.Response;
import play.mvc.Result;

public class DocuSignUtility {
	private static JsonNode authenticationHeader = Json.newObject()
			.put("Username", System.getenv(EnvironmentVariables.DOCUSIGN_EMAIL))
			.put("Password", System.getenv(EnvironmentVariables.DOCUSIGN_PASSWORD))
			.put("IntegratorKey", System.getenv(EnvironmentVariables.DOCUSIGN_INTEGRATOR));
	private static String baseUrl="";
	private static String accountId="";
	
	public static String submitDraft(Envelope env, String docUrl) throws JsonProcessingException, IOException, SmartsheetException{
		login();
		
		//Obtained after login
		Logger.info(baseUrl);
		
		//Block to create ddraft
		Promise<Response> response = initializeRequest(docUrl).get();
		Response res = response.get(60000);	
		InputStream attachmentStream = res.getBodyAsStream();
		String url = baseUrl + "/envelopes";
		String body = Json.toJson(env).toString();
		HttpURLConnection conn = InitializeRequest(url, "POST");
		byte[] bytes = IOUtils.toByteArray(attachmentStream);
		attachmentStream.close();
		
		String requestBody = "\r\n\r\n--BOUNDARY\r\n" + 
				"Content-Type: application/json\r\n" + 
				"Content-Disposition: form-data\r\n" + 
				"\r\n" + 
				body + "\r\n\r\n--BOUNDARY\r\n" + 	// our JSON formatted request body
				"Content-Type: application/pdf\r\n" + 
				"Content-Disposition: file; filename=\"" + env.getDocuments().get(0).getName() + "\"; documentId=1\r\n" + 
				"\r\n";
		String reqBody2 = "\r\n" + "--BOUNDARY--\r\n\r\n";
		// write the body of the request...S
		DataOutputStream dos = new DataOutputStream( conn.getOutputStream() );
		dos.writeBytes(requestBody.toString()); 
		dos.write(bytes);
		dos.writeBytes(reqBody2.toString()); 
		dos.flush(); dos.close();
		
		Logger.info("Sending create draft request on document...\n");
		
		int status=conn.getResponseCode(); // triggers the request
		if( status != 201 )	// 201 = Created
		{
			errorParse(conn, status);
			return null;
		}
		JsonNode json = new ObjectMapper().readTree(new StringReader(getResponseBody(conn)));
		Logger.info("Draft Created successfully");
		
		//Set new details to envelope object
		env.setEnvelopeId(json.get("envelopeId").textValue());
		env.setDateSent(new Date(System.currentTimeMillis()));
		env.setDateLastUpdated(new Date(System.currentTimeMillis()));
		
		String urlToken = getTagSendUrl(baseUrl+json.get("uri").textValue()+"/views/sender");
		
		Logger.info("URL to tag and send: "+urlToken);
		return urlToken;
	}
	
	private static void login() throws JsonProcessingException, IOException{
		Logger.info(System.getenv(EnvironmentVariables.DOCUSIGN_LOGIN_URL));
		Promise<Response> response = initializeRequest(System.getenv(EnvironmentVariables.DOCUSIGN_LOGIN_URL)).get();
		Response res = response.get(60000);	
		JsonNode json = new ObjectMapper().readTree(new StringReader(res.getBody()));
		baseUrl = json.get("loginAccounts").get(0).get("baseUrl").textValue();
		accountId = json.get("loginAccounts").get(0).get("accountId").textValue();
	}
	
	public static WSRequestHolder initializeRequest(String url) throws UnsupportedEncodingException, MalformedURLException {
		WSRequestHolder conn = WS.url(url);
		conn.setHeader("X-DocuSign-Authentication", authenticationHeader.toString());
		conn.setHeader("Accept", "application/json");
		splitQuery(conn);
		return conn;
	}
	
	public static WSRequestHolder splitQuery(WSRequestHolder request) throws UnsupportedEncodingException, MalformedURLException {
		URL url = new URL(request.getUrl());
	    String query = url.getQuery();
	    if(null != query){
	    	String[] pairs = query.split("&");
		    for (String pair : pairs) {
		        int idx = pair.indexOf("=");
		        request.setQueryParameter(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
		    }
	    }
	    return request;
	}
	public static HttpURLConnection InitializeRequest(String url, String method) {
		HttpURLConnection conn = null;
		try {
			conn = (HttpURLConnection)new URL(url).openConnection();
			
			conn.setRequestMethod(method);
			conn.setRequestProperty("X-DocuSign-Authentication", authenticationHeader.toString());
			conn.setRequestProperty("Accept", "application/json");
			if (method.equalsIgnoreCase("POST"))
			{
				conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=BOUNDARY");
				conn.setDoOutput(true);
			}
			else {
				conn.setRequestProperty("Content-Type", "application/json");
			}
			return conn;
			
		} catch (Exception e) {
	        	throw new RuntimeException(e); 
	    }
	}
	
	public static String getTagSendUrl(String url) {
		HttpURLConnection conn = null;
		String body="{\"returnUrl\": \"http://www.docusign.com/devcenter\" }";
		try {
			conn = (HttpURLConnection)new URL(url).openConnection();
			
			conn.setRequestMethod("POST");
			conn.setRequestProperty("X-DocuSign-Authentication", authenticationHeader.toString());
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestProperty("Accept", "application/json");
			conn.setRequestProperty("Content-Length", Integer.toString(body.length()));
			conn.setDoOutput(true);
			// write body of the POST request  
			DataOutputStream dos = new DataOutputStream( conn.getOutputStream() );
			dos.writeBytes(body); dos.flush(); dos.close();
			int status=conn.getResponseCode(); 
			if( status != 201 )	// 201 = Created
			{
				errorParse(conn, status);
				return "";
			}
			JsonNode json = new ObjectMapper().readTree(new StringReader(getResponseBody(conn)));
			Logger.info(json.toString());
			return json.get("url").asText();
			
		} catch (Exception e) {
	        	throw new RuntimeException(e); // simple exception handling, please review it
	    }
	}

	public static String getResponseBody(HttpURLConnection conn) {
		BufferedReader br = null;
		StringBuilder body = null;
		String line = "";
		try {
 			br = new BufferedReader(new InputStreamReader( conn.getInputStream()));
 			body = new StringBuilder();
 			while ( (line = br.readLine()) != null)
 				body.append(line);
 			return body.toString();
		} catch (Exception e) {
	        	throw new RuntimeException(e); 
	    }
	}
	
	public static void errorParse(HttpURLConnection conn, int status) { 
		BufferedReader br;
		String line;
		StringBuilder responseError;
		try {
			System.out.print("API call failed, status returned was: " + status);
			InputStreamReader isr = new InputStreamReader( conn.getErrorStream() );
			br = new BufferedReader(isr);
			responseError = new StringBuilder();
			line = null;
			while ( (line = br.readLine()) != null)
				responseError.append(line);
			Logger.info("\nError description:  " + responseError);
			return;
		}
		catch (Exception e) {
			throw new RuntimeException(e); // simple exception handling, please review it
		}
	}
}
