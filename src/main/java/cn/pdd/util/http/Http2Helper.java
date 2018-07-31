/**
 * 
 */
package cn.pdd.util.http;

import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.internal.Util;

/**
 * @author paddingdun
 * 基于okhttp3实现的工具类;
 * 注意:该类对于参数的默认编码就是:"utf-8"
 * 
 * 该类每次使用都应该重新创建Http2Helper对象(这样就可以多线程使用);
 * 切记一个对象多次调用
 * 2018年7月3日
 * @since 1.0
 * @version 1.0
 */
@SuppressWarnings({"unused"})
public class Http2Helper {
	
	/**
	 * Http2Helper 日志变量;
	 */
	private final static Logger logger = Logger.getLogger(Http2Helper.class);

	private static OkHttpClient client = null;
	
	static {
		ConnectionPool connectionPool = new ConnectionPool(5, 1, TimeUnit.MINUTES);
		//dispatcher 用来控制总请求次数;
		Dispatcher dispatcher = new Dispatcher();
		dispatcher.setMaxRequests(64);
		dispatcher.setMaxRequestsPerHost(4);
		
//		HttpLoggingInterceptor interceptor =new HttpLoggingInterceptor(HttpLoggingInterceptor.Logger.DEFAULT);
//		interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
		
		client = new OkHttpClient.Builder()
				.connectionPool(connectionPool)
				.dispatcher(dispatcher)
//				.addNetworkInterceptor(interceptor)
				.connectTimeout(60, TimeUnit.SECONDS)//连接超时时间
		        .readTimeout(60, TimeUnit.SECONDS)//读的时间
		        .writeTimeout(60, TimeUnit.SECONDS)//写的时间
				.build();
	}
	
	private Request request = null;
	private Request.Builder requestBuilder = null;
	
	private RequestBody requestBody = null;
	private FormBody.Builder formBodyBuilder = null;

	
	
	private int type;
	
	private Http2Helper(String url, int type, Charset charset){
		this.type = type;
		this.requestBuilder = new Request.Builder();
		this.requestBuilder.url(url);
		this.formBodyBuilder = new FormBody.Builder(charset);
	}
	
	/**
	 * 参数默认将是utf-8编码
	 * @param url
	 * @return
	 */
	public static Http2Helper post(String url) {
        return new Http2Helper(url, 1, Util.UTF_8);
    }
	
	public static Http2Helper post(String url, Charset charset) {
        return new Http2Helper(url, 1, charset);
    }
	
	public static Http2Helper get(String url) {
        return new Http2Helper(url, 2, Util.UTF_8);
    }
	
	public Http2Helper setContentType(final String mimeType) {
		this.requestBuilder.header("Content-Type", mimeType);
        return this;
    }
	
	public Http2Helper addHeader(final String name, final String value) {
		this.requestBuilder.addHeader(name, value);
        return this;
    }
	
	public Http2Helper setHeader(final String name, final String value) {
		this.requestBuilder.header(name, value);
        return this;
    }
	
	/**
	 * 参数和值未经过urlencode编码;
	 * @param name
	 * @param value
	 * @return
	 */
	public Http2Helper addParameter(final String name, final String value) {
		formBodyBuilder.add(name, value);
        return this;
    }
	
	/**
	 * 参数和值已经过urlencode编码;
	 * @param name
	 * @param value
	 * @return
	 */
	public Http2Helper addEncodedParameter(final String name, final String value) {
		formBodyBuilder.addEncoded(name, value);
        return this;
    }
	
	/**
	 * utf-8转成字节流;
	 * @param json
	 * @return
	 */
	public Http2Helper setParameterJson(String json) {
		this.requestBody = RequestBody.create(null, json);
        return this;
    }
	
	/**
	 * 以指定的编码方式转成字节流;
	 * @param json
	 * @param charset
	 * @return
	 */
	public Http2Helper setParameterJson(String json, Charset charset) {
		String c_name = charset.name();
		this.requestBody = RequestBody.create(MediaType.parse("application/json;charset=" + c_name), json);
        return this;
    }
	
	public Response execute()throws Exception {
		if( this.type == 1 ) {
			if(null == this.requestBody) {
				this.requestBody = this.formBodyBuilder.build();
			}
			this.request = this.requestBuilder.post(requestBody).build();
		}else if(this.type == 2) {
			this.request = this.requestBuilder.get().build();
		}
		
		if(this.request != null) {
			return client.newCall(this.request).execute();
		}else {
			throw new RuntimeException("无效的请求类型!");
		}
	}
	
	public static void destroy() {
		if(null != client) {
			try {
				client.connectionPool().evictAll();
				client.dispatcher().executorService().shutdown();
			}catch(Exception e) {
				
			}
		}
	}
	
	public static void main(String[] args) throws Exception{
	}
}
