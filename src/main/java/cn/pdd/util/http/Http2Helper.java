/**
 *
 */
package cn.pdd.util.http;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import okhttp3.ConnectionPool;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.Dispatcher;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.Util;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * modify by 2020/6/23
 * 增加保存cookie方式;
 *
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

	private final static int DEFAULT_TIMEOUT = -1;

	private static OkHttpClient client = null;

	static class MemoryCookieJar implements CookieJar{

		private List<Cookie> copy = null;

		public MemoryCookieJar(List<Cookie> cookieList) {
			this.copy = cookieList;
		}

		@Override
		public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
			copy.addAll(cookies);
		}

		@Override
		public List<Cookie> loadForRequest(HttpUrl url) {
			// 过期的Cookie
			List<Cookie> invalidCookies = new ArrayList<Cookie>();
			// 有效的Cookie
			List<Cookie> validCookies = new ArrayList<Cookie>();

			for (Cookie cookie : copy) {
				if (cookie.expiresAt() < System.currentTimeMillis()) {
					// 判断是否过期
					invalidCookies.add(cookie);
				} else if (cookie.matches(url)) {
					// 匹配Cookie对应url
					validCookies.add(cookie);
				}
			}

			// 缓存中移除过期的Cookie
			copy.removeAll(invalidCookies);
			// 返回List<Cookie>让Request进行设置
			return validCookies;
		}

	}

	static {
		ConnectionPool connectionPool = new ConnectionPool(5, 1, TimeUnit.MINUTES);
		//dispatcher 用来控制总请求次数;
		Dispatcher dispatcher = new Dispatcher();
		dispatcher.setMaxRequests(64);
		dispatcher.setMaxRequestsPerHost(4);

		HttpLoggingInterceptor interceptor =new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
			@Override
			public void log(String message) {
				if(logger.isDebugEnabled()) {
					logger.debug(message);
				}
			}
		});
		interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

//		final HashMap<String, List<Cookie>> cookieStore = new HashMap<>();
		client = new OkHttpClient.Builder()
				.connectionPool(connectionPool)
				.dispatcher(dispatcher)
				.addNetworkInterceptor(interceptor)
				.connectTimeout(10, TimeUnit.SECONDS)//连接超时时间
		        .readTimeout(10, TimeUnit.SECONDS)//读的时间
		        .writeTimeout(10, TimeUnit.SECONDS)//写的时间
//		        .cookieJar(new CookieJar() {
//			        @Override
//			        public void saveFromResponse(HttpUrl httpUrl, List<Cookie> list) {
//			            cookieStore.put(httpUrl.host(), list);
//			        }
//
//			        @Override
//			        public List<Cookie> loadForRequest(HttpUrl httpUrl) {
//			            List<Cookie> cookies = cookieStore.get(httpUrl.host());
//			            return cookies != null ? cookies : new ArrayList<Cookie>();
//			        }
//			    })
				.build();


	}

	private Request request = null;
	private Request.Builder requestBuilder = null;

	private RequestBody requestBody = null;
	private FormBody.Builder formBodyBuilder = null;
	private MultipartBody.Builder multipartBodyBuilder = null;
	private boolean multipart = false;



	private int type;
	private String url;

	/**
	 * add by 2020/6/23
	 */
	private boolean useCookie;
	private List<Cookie> cookieList;

	private Http2Helper(String url, int type, Charset charset, boolean useCookie){
		this.url = url;
		this.type = type;
		this.requestBuilder = new Request.Builder();
		this.formBodyBuilder = new FormBody.Builder(charset);
		this.multipartBodyBuilder = new MultipartBody.Builder();
		this.multipartBodyBuilder.setType(MultipartBody.FORM);
		this.useCookie = useCookie;
	}

	/**
	 * 参数默认将是utf-8编码
	 * @param url
	 * @return
	 */
	public static Http2Helper post(String url) {
        return post(url, Util.UTF_8, false);
    }

	public static Http2Helper post(String url, Charset charset, boolean useCookie) {
        return new Http2Helper(url, 1, charset, useCookie);
    }

	public static Http2Helper get(String url) {
        return get(url, false);
    }

	public static Http2Helper get(String url, boolean useCookie) {
        return new Http2Helper(url, 2, Util.UTF_8, useCookie);
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
		this.requestBody = RequestBody.create(MediaType.parse("application/json; charset=" + c_name), json);
        return this;
    }

	/**
	 * add by 2018-12-05
	 * @param name
	 * @param value
	 * @return
	 */
	public Http2Helper addPart(String name, String value) {
		this.multipartBodyBuilder.addFormDataPart(name, value);
		this.multipart = true;
		return this;
	}

	public Http2Helper addPart(String name, String filename, File file) {
		RequestBody partRequestBody = RequestBody.create(null, file);
		this.multipartBodyBuilder.addFormDataPart(name, filename, partRequestBody);
		this.multipart = true;
	    return this;
	}

	public String executeToString()throws Exception {
		return executeToString(DEFAULT_TIMEOUT, DEFAULT_TIMEOUT, DEFAULT_TIMEOUT, null, true);
	}

	public String executeToString(Charset charset)throws Exception {
		return executeToString(DEFAULT_TIMEOUT, DEFAULT_TIMEOUT, DEFAULT_TIMEOUT, charset, true);
	}

	public String executeToString(int connectTimeout, int readTimeout, int writeTimeout, Charset charset, boolean followRedirects)throws Exception {
		Response response = null;
		try {
			response = this.execute(connectTimeout, readTimeout, writeTimeout, followRedirects);
			int code = response.code();
			if(code == 200) {
				ResponseBody rb = response.body();
				if(null != charset) {
					return new String(rb.bytes(), charset);
				}
				return rb.string();
			}
			throw new RuntimeException("HTTP CODE:" + code);
		}finally {
			if(null != response) {
				response.close();
			}
		}
	}

	/**
	 * 单位:秒;
	 * @param connectTimeout
	 * @param readTimeout
	 * @param writeTimeout
	 * @return
	 * @throws Exception
	 */
	public Response execute(int connectTimeout, int readTimeout, int writeTimeout, boolean followRedirects)throws Exception {
		if( this.type == 1 ) {
			if(this.multipart) {
				this.requestBody = this.multipartBodyBuilder.build();
			}else {
				if(null == this.requestBody) {
					this.requestBody = this.formBodyBuilder.build();
				}
			}
			this.request = this.requestBuilder.url(this.url).post(requestBody).build();
		}else if(this.type == 2) {
			/**
			 * add by 2019-11-19
			 * 如果增加了formBody参数,将其拼接到url上;
			 */
			FormBody fb = this.formBodyBuilder.build();
			int qs = fb.size();
			HttpUrl hu = HttpUrl.parse(this.url);
			if(qs > 0) {
				HttpUrl.Builder hub = hu.newBuilder();
				for (int i = 0; i < qs; i++) {
					hub.addQueryParameter(fb.name(i), fb.value(i));
				}
				hu = hub.build();
			}
			this.request = this.requestBuilder.url(hu).get().build();
		}


		if(this.request != null) {
			OkHttpClient c = null;
			if(connectTimeout == DEFAULT_TIMEOUT
					&& readTimeout == DEFAULT_TIMEOUT
					&& writeTimeout == DEFAULT_TIMEOUT
					&& followRedirects == true) {
				if(useCookie) {//需要cookie;
					MemoryCookieJar mcj = new MemoryCookieJar(this.cookieList);
					c = client.newBuilder()
							.cookieJar(mcj)
							.build();
				}else {
					c = client;
				}
			}else {
				Util.checkDuration("connectTimeout", connectTimeout, TimeUnit.SECONDS);
				Util.checkDuration("readTimeout", readTimeout, TimeUnit.SECONDS);
				Util.checkDuration("writeTimeout", writeTimeout, TimeUnit.SECONDS);

				OkHttpClient.Builder b = client.newBuilder();
				b.followRedirects(followRedirects)
				.connectTimeout(connectTimeout, TimeUnit.SECONDS)//连接超时时间
		        .readTimeout(readTimeout, TimeUnit.SECONDS)//读的时间
		        .writeTimeout(writeTimeout, TimeUnit.SECONDS);//写的时间

				if(useCookie) {//需要cookie;
					MemoryCookieJar mcj = new MemoryCookieJar(this.cookieList);
					b.cookieJar(mcj);
				}
		        c = b.build();
			}
			Response result = c.newCall(this.request).execute();
			c = null;
			return result;
		}else {
			throw new RuntimeException("无效的请求类型!");
		}
	}

	public Response execute()throws Exception {
		return execute(DEFAULT_TIMEOUT, DEFAULT_TIMEOUT, DEFAULT_TIMEOUT, true);
	}

	public static void destroy() {
		if(null != client) {
			try {
				client.connectionPool().evictAll();
				client.dispatcher().executorService().shutdown();
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) throws Exception{
		Http2Helper helper = Http2Helper.get("http://www.baidu55.com");
		String s = helper.executeToString(5,1,1, null, false);
		System.out.println(s);
	}
}
