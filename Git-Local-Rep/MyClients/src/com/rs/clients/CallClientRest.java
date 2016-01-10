package com.rs.clients;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

import com.orbitz.consul.Consul;
import com.orbitz.consul.HealthClient;
import com.orbitz.consul.model.health.ServiceHealth;

// exec:java -Dexec.mainClass="com.rs.clients.CallClientRest"
public class CallClientRest {
/**
 * http://localhost:8081/hystrix-dashboard-1.4.9/
 * http://localhost:8081/WebClient-1.0/hystrix.stream
 * @param args
 */
	public static void main(String[] args) {
		sendRequestToServiceUsingConsul();
		/*
		try {
		for(int i = 0; i < 5000; i++) {
			callClientService();
			Thread.sleep(1);
		}
		} catch (Exception e) {}
		*/
	}
	
	public static void sendRequestToServiceUsingConsul() {
		Consul consul = Consul.builder().build(); // connect to Consul on localhost
		HealthClient healthClient = consul.healthClient();

		List<ServiceHealth> nodes = healthClient.getHealthyServiceInstances("Service1").getResponse(); 
		ServiceHealth sh = null;
		if(nodes != null && nodes.size() > 0) {
			sh = nodes.get(0);
		}
		System.out.println("getNode: " + sh.getNode());
	}

	public static void callClientService() {
		try {
			HttpClient httpClient = HttpClientBuilder.create().build();
			

			//HttpGet getRequest = new HttpGet("http://localhost:8080/WebClient/clientRS/callService/query?s=ee");
			HttpGet getRequest = new HttpGet("http://localhost:8081/WebClient-1.0/clientRS/callService/query?s=ee");
			getRequest.addHeader("accept", "application/xml");

			HttpResponse response = httpClient.execute(getRequest);

			// Check for HTTP response code: 200 = success
			if (response.getStatusLine().getStatusCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : " + response.getStatusLine().getStatusCode());
			}

			// Get-Capture Complete application/xml body response
			BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));

			String line;
			String output = "";
			while ((line = br.readLine()) != null) {
				output = output + line;
			}
			System.out.println("out: " + output);
		} catch (ClientProtocolException e) {
			e.printStackTrace();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
