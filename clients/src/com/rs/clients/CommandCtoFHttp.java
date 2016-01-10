
package com.rs.clients;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;


public class CommandCtoFHttp extends HystrixCommand<String> {

    private final String name;

    public CommandCtoFHttp(String name) {
    	super(HystrixCommandGroupKey.Factory.asKey("CommandTempGroup"));

        /*
        super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("CommandHttpClient"))
                .andCommandPropertiesDefaults(HystrixCommandProperties.Setter().
                		withExecutionIsolationThreadTimeoutInMilliseconds(4000)));*/
        
        this.name = name;
    }

    @Override
    protected String run() {
    	String output = "";
    	try {
    		
			// create HTTP Client
			HttpClient httpClient = HttpClientBuilder.create().build();
 
			// Create new getRequest with below mentioned URL
			//HttpGet getRequest = new HttpGet("http://localhost:8080/RestService1/RS/ctofservice/query?c=50&error=false");
			HttpGet getRequest = new HttpGet("http://localhost:8081/RestService1-0.0.1-SNAPSHOT/RS/ctofservice/query?c=110&error=false");
 
			// Add additional header to getRequest which accepts application/xml data
			getRequest.addHeader("accept", "application/xml");
 
			// Execute your request and catch response
			HttpResponse response = httpClient.execute(getRequest);
 
			// Check for HTTP response code: 200 = success
			if (response.getStatusLine().getStatusCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : " + response.getStatusLine().getStatusCode());
			}
 
			// Get-Capture Complete application/xml body response
			BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
			
			
			String line;
			// Simply iterate through XML response and show on console.
			while ((line = br.readLine()) != null) {
				output = output + line;
			}
 
		} catch (ClientProtocolException e) {
			e.printStackTrace();
 
		} catch (IOException e) {
			e.printStackTrace();
		}
        return output;
    }

    @Override
    protected String getFallback() {
        return "####Fallback CommandCtoFHttp";
    }
}