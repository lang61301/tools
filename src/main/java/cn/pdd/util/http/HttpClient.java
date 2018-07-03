/**
 * 
 */
package cn.pdd.util.http;

import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;

/**
 * @author paddingdun
 *
 * 2018年7月3日
 * @since 1.0
 * @version 1.0
 */
public class HttpClient {
	
	/**
	 * HttpClient 日志变量;
	 */
	
	private final static Logger logger = Logger.getLogger(HttpClient.class);
	
	private static HttpClient instance;
	
	private OkHttpClient client = null;
	
	private HttpClient() {
		init();
	}
	
	private void init() {
		ConnectionPool connectionPool = new ConnectionPool(5, 1, TimeUnit.MINUTES);
		//dispatcher 用来控制总请求次数;
		Dispatcher dispatcher = new Dispatcher();
		dispatcher.setMaxRequests(64);
		dispatcher.setMaxRequestsPerHost(4);
		client = new OkHttpClient.Builder()
				.connectionPool(connectionPool)
				.dispatcher(dispatcher)
				.connectTimeout(60, TimeUnit.SECONDS)//连接超时时间
		        .readTimeout(60, TimeUnit.SECONDS)//读的时间
		        .writeTimeout(60, TimeUnit.SECONDS)//写的时间
				.build();
		logger.info("OkHttpClient client init success!");
	}

	public static synchronized HttpClient getInstance() {
		if(null == instance) {
			instance = new HttpClient();
		}
		return instance;
	}

	public OkHttpClient getClient() {
		return client;
	}
}
