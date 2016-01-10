package com.rs.clients;

import java.util.Random;
import java.util.concurrent.Future;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import com.netflix.config.ConfigurationManager;

@Path("/callService")
public class TempConverterClient {
	@GET
	@Produces("application/xml")
	public String TempConverterClient() {
		return "<ctofservice>" + "<celsius>" + "44" + "</celsius>" + "<ctofoutput>" + "88" + "</ctofoutput>" + "</ctofservice>";
	}
 
	@GET
	@Path("/query")
	@Produces("application/xml")
	public String testRestService1(@QueryParam("s") int s) {
		//http://localhost:8080/WebClient/clientRS/callService/query?s=ee
		String ctofRes = null;
		String ftocRes = null;
		
		CommandCtoFHttp ctof = new CommandCtoFHttp("");
		CommandFtoCHttp ftoc = new CommandFtoCHttp("");
		
		ConfigurationManager.getConfigInstance().setProperty("hystrix.threadpool.default.coreSize", 2);
		ConfigurationManager.getConfigInstance()
				.setProperty("hystrix.command.CommandCtoFHttp.execution.isolation.thread.timeoutInMilliseconds", 800);
		ConfigurationManager.getConfigInstance()
				.setProperty("hystrix.command.CommandFtoCHttp.execution.isolation.thread.timeoutInMilliseconds", s);
		
		
		ConfigurationManager.getConfigInstance().setProperty("hystrix.command.CommandCtoFHttp.metrics.rollingStats.timeInMilliseconds", 1000 * 10);
		ConfigurationManager.getConfigInstance().setProperty("hystrix.command.CommandFtoCHttp.metrics.rollingStats.timeInMilliseconds", 1000 * 10);
		
		ConfigurationManager.getConfigInstance().setProperty("hystrix.command.CommandCtoFHttp.circuitBreaker.enabled", true);
		ConfigurationManager.getConfigInstance().setProperty("hystrix.command.CommandFtoCHttp.circuitBreaker.enabled", true);
	
		//ConfigurationManager.getConfigInstance().setProperty("hystrix.command.CommandCtoFHttp.circuitBreaker.requestVolumeThreshold", 10);
		//ConfigurationManager.getConfigInstance().setProperty("hystrix.command.CommandFtoCHttp.circuitBreaker.requestVolumeThreshold", 10);
		
		ConfigurationManager.getConfigInstance().setProperty("hystrix.command.CommandCtoFHttp.circuitBreaker.errorThresholdPercentage", 20);
		ConfigurationManager.getConfigInstance().setProperty("hystrix.command.CommandFtoCHttp.circuitBreaker.errorThresholdPercentage", 20);

		ConfigurationManager.getConfigInstance().setProperty("hystrix.command.CommandCtoFHttp.circuitBreaker.sleepWindowInMilliseconds", 5000);
		ConfigurationManager.getConfigInstance().setProperty("hystrix.command.CommandFtoCHttp.circuitBreaker.sleepWindowInMilliseconds", 5000);
		
		try {
			Future<String> ctofFuture = ctof.queue();
			Future<String> ftocFuture = ftoc.queue();
			ctofRes = ctofFuture.get();
			ftocRes = ftocFuture.get();
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("convert C to F: " + ctofRes);
		System.out.println("convert F to C: " + ftocRes);
		return "<ctofservice>" + "<celsius>" + "33" + "</celsius>" + "<ctofoutput>" + "66" + "</ctofoutput>" + "</ctofservice>";
	}
}
