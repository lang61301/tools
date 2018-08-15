package cn.pdd.util.http;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * web工具类;
 * @author admin
 *
 */
@SuppressWarnings("unused")
public class WebHelper {
	
	/**
	 * WebHelper 日志变量;
	 */
	private final static Logger logger = Logger.getLogger(WebHelper.class);


	/**
	 * 返回ajax请求;
	 * @param response
	 * @param gson
	 * @param jr
	 * @throws IOException
	 */
	public static<T> void rtnAjax(HttpServletResponse response, String jsonStr)throws IOException{
		response.reset();
		response.setContentType("application/json;charset=UTF-8");
		PrintWriter out = response.getWriter();
		
		out.write(jsonStr);
		
		out.flush();
		out.close();
	}
	
	/**
	 * 判断是否是ajax请求;
	 * @param request
	 * @return
	 */
	public static boolean isAjaxHttprequest(HttpServletRequest request){
		boolean result = false;
		if ("XMLHttpRequest"
	            .equalsIgnoreCase(request
	                    .getHeader("X-Requested-With"))) {//ajax请求;
			result = true;
		}
		return result;
	}
	
	/**
	 * 获取访问ip地址;
	 * @param request
	 * @return
	 */
	public static String getIpAddr(HttpServletRequest request) {
		String ip = request.getHeader("X-Forwarded-For");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_CLIENT_IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_X_FORWARDED_FOR");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		return ip;
	}
	
	/**
	 * 设置response为无缓存;
	 * @param response
	 * @throws IOException
	 */
	public static void setNoCacheHeader(HttpServletResponse response)throws IOException{
		response.setDateHeader("Expires", 0);
	    response.addHeader("Pragma", "no-cache");
	    response.setHeader("Cache-Control", "no-cache, no-store, max-age=0");
	}
	
	/**
	 * 判断是否是http或https开始的url;
	 * @param url
	 * @return
	 */
	public static boolean isHttpURL(String url){
		if(StringUtils.isNotBlank(url)){
			String tmp = url.trim().toLowerCase();
			if(tmp.startsWith("http://")
					|| tmp.startsWith("https://")){
				return true;
			}
		}
		return false;
	}
}
