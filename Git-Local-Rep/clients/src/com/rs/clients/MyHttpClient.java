package com.rs.clients;

import java.util.HashMap;
import java.util.concurrent.Future;

import com.netflix.config.ConfigurationManager;
import com.netflix.hystrix.HystrixCircuitBreaker;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandMetrics;
import com.netflix.hystrix.HystrixCommandMetrics.HealthCounts;
import com.netflix.hystrix.util.HystrixRollingNumberEvent;

/**
 * @author Crunchify.com
 * 
 */

public class MyHttpClient {
	public static void main(String[] args) {		
		configCommands();
		
		try {			
			for (int i = 0; i < 500; i++) {
				// execCommand();
				queueCommand();
				Thread.sleep(200);
				
			}
		} catch (Exception e) {
		}
		printHystrixMetrics();
	}

	public static void configCommands() {
		
		ConfigurationManager.getConfigInstance().setProperty("hystrix.threadpool.default.coreSize", 2);
		ConfigurationManager.getConfigInstance()
				.setProperty("hystrix.command.CommandCtoFHttp.execution.isolation.thread.timeoutInMilliseconds", 1000);
		ConfigurationManager.getConfigInstance()
				.setProperty("hystrix.command.CommandFtoCHttp.execution.isolation.thread.timeoutInMilliseconds", 1000);
		
		ConfigurationManager.getConfigInstance().setProperty("hystrix.command.CommandCtoFHttp.metrics.rollingStats.timeInMilliseconds", 1000 * 10);
		ConfigurationManager.getConfigInstance().setProperty("hystrix.command.CommandFtoCHttp.metrics.rollingStats.timeInMilliseconds", 1000 * 10);
		
		ConfigurationManager.getConfigInstance().setProperty("hystrix.command.CommandCtoFHttp.circuitBreaker.enabled", true);
		ConfigurationManager.getConfigInstance().setProperty("hystrix.command.CommandFtoCHttp.circuitBreaker.enabled", true);
		
		ConfigurationManager.getConfigInstance().setProperty("hystrix.command.CommandCtoFHttp.circuitBreaker.requestVolumeThreshold", 5);
		ConfigurationManager.getConfigInstance().setProperty("hystrix.command.CommandFtoCHttp.circuitBreaker.requestVolumeThreshold", 5);
		
		//ConfigurationManager.getConfigInstance().setProperty("hystrix.command.CommandCtoFHttp.circuitBreaker.errorThresholdPercentage", 10);
		//ConfigurationManager.getConfigInstance().setProperty("hystrix.command.CommandFtoCHttp.circuitBreaker.errorThresholdPercentage", 10);

		ConfigurationManager.getConfigInstance().setProperty("hystrix.command.CommandCtoFHttp.circuitBreaker.sleepWindowInMilliseconds", 5000);
		ConfigurationManager.getConfigInstance().setProperty("hystrix.command.CommandFtoCHttp.circuitBreaker.sleepWindowInMilliseconds", 5000);

	}

	public static void execCommand() {
		String ctofRes = null;
		String ftocRes = null;
		
		CommandCtoFHttp ctof = new CommandCtoFHttp("");
		CommandFtoCHttp ftoc = new CommandFtoCHttp("");
		
		long st = System.currentTimeMillis();
		System.out.println("\ninvoking the services synchronously:");
		try {
			ctofRes = ctof.execute();
			ftocRes = ftoc.execute();
		} catch (Exception e) {
			e.printStackTrace();
		}
		long et = System.currentTimeMillis();
		long time = et - st;
		//System.out.println("output:");
		System.out.println("convert C to F: " + ctofRes);
		System.out.println("convert F to C: " + ftocRes);
		System.out.println("**** synchronous response time: " + time + " ****");
	}

	public static void queueCommand() {
		String ctofRes = null;
		String ftocRes = null;
		
		CommandCtoFHttp ctof = new CommandCtoFHttp("");
		CommandFtoCHttp ftoc = new CommandFtoCHttp("");
		
		if(ctof.isCircuitBreakerOpen()) {
			System.out.println("@@@@ C to F service circuit is open " + " @@@@");
		}
		if(ftoc.isCircuitBreakerOpen()) {
			System.out.println("@@@@ F to C service circuit is open " + " @@@@");
		}
		
		long st = System.currentTimeMillis();
		System.out.println("\ninvoking the services asynchronously:");

		try {
			Future<String> ctofFuture = ctof.queue();
			Future<String> ftocFuture = ftoc.queue();
			// System.out.println("after queue");
			ctofRes = ctofFuture.get();
			ftocRes = ftocFuture.get();
			// System.out.println("after get");
		} catch (Exception e) {
			e.printStackTrace();
		}
		long et = System.currentTimeMillis();
		long time = et - st;

	//	System.out.println("output:");
		System.out.println("convert C to F: " + ctofRes);
		System.out.println("convert F to C: " + ftocRes);
		System.out.println("**** asynchronous response time: " + time + " ****");
	}
	
	public static void printHystrixMetrics() {
		HystrixCommandMetrics ctofMetrics = HystrixCommandMetrics.getInstance(HystrixCommandKey.Factory.asKey("CommandCtoFHttp"));
		System.out.println("CommandCtoFHttp metrics: ");
		printMetrics(ctofMetrics);
		
		HystrixCommandMetrics ftocMetrics = HystrixCommandMetrics.getInstance(HystrixCommandKey.Factory.asKey("CommandFtoCHttp"));
		System.out.println("\nCommandFtoCHttp metrics: ");
		printMetrics(ftocMetrics);

	}
	
	public static void printMetrics(HystrixCommandMetrics metrics) {
		HashMap metricsMap = new HashMap();
		if (metrics != null) {
			HealthCounts counts = metrics.getHealthCounts();
			//HystrixCircuitBreaker circuitBreaker = HystrixCircuitBreaker.Factory.getInstance(HystrixCommandKey.Factory.asKey("CommandCtoFHttp"));
			
			//metricsMap.put("circuitOpen", circuitBreaker.isOpen());
			//metricsMap.put("totalRequest", counts.getTotalRequests());
			metricsMap.put("errorPercentage", counts.getErrorPercentage());
			metricsMap.put("success", metrics.getRollingCount(HystrixRollingNumberEvent.SUCCESS));
			metricsMap.put("timeout", metrics.getRollingCount(HystrixRollingNumberEvent.TIMEOUT));
			metricsMap.put("failure", metrics.getRollingCount(HystrixRollingNumberEvent.FAILURE));
			//metricsMap.put("shortCircuited", metrics.getRollingCount(HystrixRollingNumberEvent.SHORT_CIRCUITED));
			//metricsMap.put("threadPoolRejected",
			//metrics.getRollingCount(HystrixRollingNumberEvent.THREAD_POOL_REJECTED));
			//metricsMap.put("semaphoreRejected", ctofMetrics.getRollingCount(HystrixRollingNumberEvent.SEMAPHORE_REJECTED));
			//metricsMap.put("latency50", metrics.getTotalTimePercentile(50));
			//metricsMap.put("latency90", metrics.getTotalTimePercentile(90));
			//metricsMap.put("latency100", metrics.getTotalTimePercentile(100));
		}

		System.out.println("errorPercentage: " + metricsMap.get("errorPercentage"));
		System.out.println("success: " + metricsMap.get("success"));
		System.out.println("timeout: " + metricsMap.get("timeout"));
		System.out.println("failure: " + metricsMap.get("failure"));
	}
}
