/**
 * 
 */
package cn.pdd.util.servlet;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * 感谢zss.org; 2016年3月22日
 * 
 * @since 1.0
 * @version 1.0
 */
@SuppressWarnings({ "static-access", "rawtypes", "unchecked" })
public class Servlets {

	private static final Pattern _rwebkit = Pattern.compile(".*(webkit)[ /]([\\w.]+).*"),
			_ropera = Pattern.compile(".*(opera)(?:.*version)?[ /]([\\w.]+).*"),
			_rmsie = Pattern.compile(".*(msie) ([\\w.]+).*"),
			_rmozilla = Pattern.compile(".*(mozilla)(?:.*? rv:([\\w.]+))?.*"),
			_rchrome = Pattern.compile(".*(chrome)[ /]([\\w.]+).*"),
			_randroid = Pattern.compile(".*(android)[ /]([\\w.]+).*"),
			_rsafari = Pattern.compile(".*(safari)[ /]([\\w.]+).*"), _rtrident = Pattern.compile("trident/([0-9\\.]+)");

	private static final boolean _svl24, _svl23, _svl3;
	static {
		boolean b = false;
		try {
			HttpSession.class.forName("javax.servlet.annotation.WebServlet");
			b = true;
		} catch (Throwable ex) {
		}
		_svl3 = b;

		if (!b) {
			try {
				ServletResponse.class.getMethod("getContentType", new Class[0]);
				b = true;
			} catch (Throwable ex) {
			}
		}
		_svl24 = b;

		if (!b) {
			try {
				HttpSession.class.getMethod("getServletContext", new Class[0]);
				b = true;
			} catch (Throwable ex) {
			}
		}
		_svl23 = b;
	}

	/** Utilities; no instantiation required. */
	protected Servlets() {
	}

	/**
	 * Returns whether a URL starts with xxx://, mailto:, about:, javascript:, data:
	 */
	public static final boolean isUniversalURL(String uri) {
		if (uri == null || uri.length() == 0)
			return false;

		final char cc = uri.charAt(0);
		return cc >= 'a' && cc <= 'z' && (uri.indexOf("://") > 0 || uri.startsWith("mailto:")
				|| uri.startsWith("javascript:") || uri.startsWith("about:") || uri.startsWith("data:"));
	}

	/**
	 * Returns whether the current Web server supports Servlet 3.0 or above.
	 *
	 * @since 6.0.0
	 */
	public static final boolean isServlet3() {
		return _svl3;
	}

	/**
	 * Returns whether the current Web server supports Servlet 2.4 or above.
	 *
	 * @since 3.0.0
	 */
	public static final boolean isServlet24() {
		return _svl24;
	}

	/**
	 * Returns whether the current Web server supports Servlet 2.3 or above. Thus,
	 * if {@link #isServlet24} returns true, {@link #isServlet23} must return true,
	 * too.
	 *
	 * @since 3.0.0
	 */
	public static final boolean isServlet23() {
		return _svl23;
	}

	/**
	 * Returns the version of the given browser name, or null if the client is not
	 * the given browsers.
	 *
	 * <p>
	 * Notice that, after this method is called, an attribute named zk will be
	 * stored to the request, such that you can retrieve the browser information by
	 * use of EL, such as <code>${zk.ie > 7}</code>.
	 *
	 * @param request
	 *            the request.
	 * @param name
	 *            the browser's name. It includes "ie", "ff", "gecko", "webkit",
	 *            "safari", "opera", "android", "mobile", "ios", "iphone", "ipad"
	 *            and "ipod". And, "ff" is the same as "gecko", and "webkit" is the
	 *            same as "safari".
	 * @since 6.0.0
	 */
	public static Double getBrowser(ServletRequest request, String name) {
		return (Double) browserInfo(request).get(name);
	}

	/**
	 * Returns the name of the browser, or null if not identifiable.
	 * 
	 * @since 6.0.0
	 */
	public static String getBrowser(ServletRequest request) {
		return (String) ((Map) browserInfo(request).get("browser")).get("name");
	}

	/**
	 * Returns the version of the given browser name, or null if the client is not
	 * the given browsers.
	 *
	 * <p>
	 * Notice that, after this method is called, an attribute named zk will be
	 * stored to the request, such that you can retrieve the browser information by
	 * use of EL, such as <code>${zk.ie > 7}</code>.
	 *
	 * @param userAgent
	 *            the user agent (i.e., the user-agent header in HTTP).
	 * @param name
	 *            the browser's name. It includes "ie", "ff", "gecko", "webkit",
	 *            "safari", "opera", "android", "mobile", "ios", "iphone", "ipad"
	 *            and "ipod". And, "ff" is the same as "gecko", and "webit" is the
	 *            same as "safari".
	 * @since 6.0.0
	 */
	public static Double getBrowser(String userAgent, String name) {
		final Map<String, Object> zk = new HashMap<String, Object>();
		browserInfo(zk, userAgent);
		return (Double) zk.get(name);
	}

	/**
	 * Returns the name of the browser, or null if not identifiable.
	 * 
	 * @param userAgent
	 *            the user agent (i.e., the user-agent header in HTTP).
	 * @since 6.0.0
	 */
	public static String getBrowser(String userAgent) {
		final Map<String, Object> zk = new HashMap<String, Object>();
		browserInfo(zk, userAgent);
		return (String) ((Map) zk.get("browser")).get("name");
	}

	private static Map browserInfo(ServletRequest request) {
		// No need synchronized since the servlet request is executed synchronously
		Map<String, Object> zk = (Map<String, Object>) request.getAttribute("zk");
		if (zk == null)
			request.setAttribute("zk", zk = new HashMap<String, Object>(4));

		if (!zk.containsKey("browser"))
			browserInfo(zk, getUserAgent(request));
		return zk;
	}

	private static void browserInfo(Map<String, Object> zk, String ua) {
		if (ua != null) {
			// ZK-1822: In locale Turkish, it can prevent 'I'.toLowerCase becomes 'i'
			// without dot.
			ua = ua.toLowerCase(Locale.ENGLISH);
			Matcher m = _rwebkit.matcher(ua);
			if (m.matches()) {
				double version;
				browserInfo(zk, "webkit", version = getVersion(m));

				// B70-ZK-2088: make sure Browser is not chrome
				boolean matchChrome = false;
				m = _rchrome.matcher(ua);
				if (m.matches()) {
					zk.put("chrome", getVersion(m));
					matchChrome = true;
				}

				m = _rsafari.matcher(ua);
				if (!matchChrome && m.matches())
					zk.put("safari", getVersion(m));

				m = _randroid.matcher(ua);
				if (m.matches()) {
					double v = getVersion(m);
					zk.put("android", v);
					zk.put("mobile", v);
				}

				for (int j = _ios.length; --j >= 0;)
					if (ua.indexOf(_ios[j]) >= 0) {
						zk.put(_ios[j], version);
						zk.put("ios", version);
						zk.put("mobile", version);
						return;
					}
				return;
			}
			m = _ropera.matcher(ua);
			if (m.matches()) {
				browserInfo(zk, "opera", getVersion(m));
				return;
			}
			m = _rmsie.matcher(ua);
			if (m.matches()) {
				browserInfo(zk, "ie", getVersion(m));
				return;
			}

			if (ua.indexOf("compatible") < 0) {
				m = _rmozilla.matcher(ua);
				if (m.matches()) {
					double version = getVersion(m);
					if (version < 5) { // http://www.useragentstring.com/_uas_Firefox_version_5.0.php
						int j = ua.indexOf("firefox/");
						if (j >= 0) {
							int k = ua.indexOf('.', j += 8);
							if (k >= 0) {
								for (int len = ua.length(); ++k < len;) {
									final char cc = ua.charAt(k);
									if (cc < '0' || cc > '9')
										break;
								}
								try {
									version = Double.parseDouble(ua.substring(j, k));
								} catch (Throwable ex) {
								}
							}
						}
					}

					// ie11~
					if (ua.contains("trident")) {
						browserInfo(zk, "ie", version);
						return;
					}

					// the version after gecko/* is confusing, so we
					// use firefox's version instead
					browserInfo(zk, "gecko", version);
					zk.put("ff", version);
					return;
				}
			}
		}
		zk.put("browser", Collections.emptyMap()); // none matched
	}

	private static final String[] _ios = { "ipod", "iphone", "ipad" };

	private static void browserInfo(Map<String, Object> zk, String name, double version) {
		final Map<String, Object> bi = new HashMap<String, Object>(4);
		bi.put("name", name);
		bi.put("version", version);
		zk.put("browser", bi);
		zk.put(name, version);
	}

	private static double getVersion(Matcher m) {
		return m.groupCount() < 2 ? 1/* ignore it */ : getVersion(m.group(2));
	}

	private static double getVersion(String version) {
		try {
			int j = version.indexOf('.');
			if (j >= 0) {
				j = version.indexOf('.', j + 1);
				if (j >= 0)
					version = version.substring(0, j);
			}
			return Double.parseDouble(version);
		} catch (Throwable t) {
			return 1; // ignore it
		}
	}

	/**
	 * Returns whether the client is a browser of the specified type.
	 *
	 * @param type
	 *            the type of the browser. The syntax:
	 *            <code>&lt;browser-name&gt;[&lt;version-number&gt];[-]</code>.<br/>
	 *            For example, ie9, ios and ie6-. And, <code>ie9</code> means
	 *            Internet Explorer 9 and later, while <code>ie6-</code> means
	 *            Internet Explorer 6 (not prior, nor later).
	 * @since 3.5.1
	 */
	public static boolean isBrowser(ServletRequest req, String type) {
		return (req instanceof HttpServletRequest) && isBrowser(getUserAgent(req), type);
	}

	/**
	 * Returns whether the user agent is a browser of the specified type.
	 *
	 * @param userAgent
	 *            represents a client. For HTTP clients, It is the user-agent
	 *            header.
	 * @param type
	 *            the type of the browser, or a list of types separated by
	 *            comma.<br/>
	 *            The syntax:
	 *            <code>&lt;browser-name&gt;[&lt;version-number&gt];[-]</code>.<br/>
	 *            For example, ie9, ios and ie6-. And, <code>ie9</code> means
	 *            Internet Explorer 9 and later, while <code>ie6-</code> means
	 *            Internet Explorer 6 (not prior, nor later).<br/>
	 *            If a list of types are specified (such as <code>ie6-,ie7-</code>),
	 *            this method returns true if any of them matches (i.e., OR
	 *            condition).
	 * @since 3.5.1
	 */
	public static boolean isBrowser(String userAgent, String type) {
		String[] types = type.split(",");
		for (int j = 0; j < types.length; ++j)
			if (browser(userAgent, types[j]))
				return true; // OR
		return false;
	}

	private static boolean browser(String userAgent, String type) {
		if (userAgent == null) // Bug ZK-1582: userAgent could be null if it is robot.
			return false;

		int last = (type = type.trim()).length();
		if (last == 0)
			return false;

		char cc = type.charAt(last - 1);
		final boolean equals = cc == '-' || cc == '_';
		if (equals || cc == '+')
			last--;

		int j;
		for (j = last; j > 0; --j)
			if ((cc = type.charAt(j - 1)) != '.' && (cc < '0' || cc > '9'))
				break;

		Double vtype = null;
		if (j < last) {
			try {
				vtype = Double.parseDouble(type.substring(j, last));
			} catch (Throwable t) {
			}
			last = j;
		}

		String btype = type.substring(0, last);
		Double vclient = getBrowser(userAgent, btype);
		if (vclient == null && (userAgent.indexOf(btype + ("ie".equals(btype) ? ' ' : "")) < 0)) // Bug ZK-1289: for
																									// Viewpad and IE
																									// mixed bug
			return false; // not matched
		if (vtype == null)
			return true; // not care about version

		if (vclient == null)
			return false; // not matched for Bug ZK-1930

		double v1 = vclient.doubleValue(), v2 = vtype.doubleValue();
		return equals ? v1 == v2 : v1 >= v2;
	}

	/**
	 * Returns whether the client is a robot (such as Web crawlers).
	 *
	 * <p>
	 * Because there are too many robots, it returns true if the user-agent is not
	 * recognized.
	 * 
	 * @deprecated As of release 6.0.0, replaced with {@link #getBrowser}.
	 */
	public static final boolean isRobot(ServletRequest req) {
		return (req instanceof HttpServletRequest) && isRobot(getUserAgent(req));
	}

	/**
	 * Returns whether the client is a robot (such as Web crawlers).
	 *
	 * <p>
	 * Because there are too many robots, it returns true if the user-agent is not
	 * recognized.
	 *
	 * @param userAgent
	 *            represents a client. For HTTP clients, It is the user-agent
	 *            header.
	 * @since 3.5.1
	 * @deprecated As of release 6.0.0, replaced with {@link #getBrowser}.
	 */
	public static final boolean isRobot(String userAgent) {
		if (userAgent == null)
			return false;

		boolean ie = userAgent.indexOf("MSIE ") >= 0;
		// Bug 3107026: in Turkish, "MSIE".toLowerCase(java.util.Locale.ENGLISH) is NOT
		// "msie"
		userAgent = userAgent.toLowerCase(java.util.Locale.ENGLISH);
		return !ie && userAgent.indexOf("msie ") < 0 && userAgent.indexOf("opera") < 0
				&& userAgent.indexOf("gecko/") < 0 && userAgent.indexOf("safari") < 0 && userAgent.indexOf("zk") < 0
				&& userAgent.indexOf("rmil") < 0;
	}

	/**
	 * Returns whether the browser is Internet Explorer. If true, it also implies
	 * {@link #isExplorer7} is true.
	 * 
	 * @deprecated As of release 6.0.0, replaced with {@link #getBrowser}.
	 */
	public static final boolean isExplorer(ServletRequest req) {
		return (req instanceof HttpServletRequest) && isExplorer(getUserAgent(req));
	}

	/**
	 * Returns whether the browser is Internet Explorer. If true, it also implies
	 * {@link #isExplorer7} is true.
	 *
	 * @param userAgent
	 *            represents a client. For HTTP clients, It is the user-agent
	 *            header.
	 * @since 3.5.1
	 * @deprecated As of release 6.0.0, replaced with {@link #getBrowser}.
	 */
	public static final boolean isExplorer(String userAgent) {
		if (userAgent == null)
			return false;

		boolean ie = userAgent.indexOf("MSIE ") >= 0;
		// Bug 3107026: in Turkish, "MSIE".toLowerCase(java.util.Locale.ENGLISH) is NOT
		// "msie"
		userAgent = userAgent.toLowerCase(java.util.Locale.ENGLISH);
		return (ie || userAgent.indexOf("msie ") >= 0) && userAgent.indexOf("opera") < 0;
	}

	/**
	 * Returns whether the browser is Explorer 7 or later.
	 * 
	 * @deprecated As of release 6.0.0, replaced with {@link #getBrowser}.
	 */
	public static final boolean isExplorer7(ServletRequest req) {
		return (req instanceof HttpServletRequest) && isExplorer7(getUserAgent(req));
	}

	/**
	 * Returns whether the browser is Explorer 7 or later.
	 *
	 * @param userAgent
	 *            represents a client. For HTTP clients, It is the user-agent
	 *            header.
	 * @since 3.5.1
	 * @deprecated As of release 6.0.0, replaced with {@link #getBrowser}.
	 */
	public static final boolean isExplorer7(String userAgent) {
		return isBrowser(userAgent, "ie7");
	}

	/**
	 * Returns whether the browser is Gecko based, such as Mozilla, Firefox and
	 * Camino If true, it also implies {@link #isGecko3} is true.
	 * 
	 * @deprecated As of release 6.0.0, replaced with {@link #getBrowser}.
	 */
	public static final boolean isGecko(ServletRequest req) {
		return (req instanceof HttpServletRequest) && isGecko(getUserAgent(req));
	}

	/**
	 * Returns whether the browser is Gecko based, such as Mozilla, Firefox and
	 * Camino If true, it also implies {@link #isGecko3} is true.
	 *
	 * @param userAgent
	 *            represents a client. For HTTP clients, It is the user-agent
	 *            header.
	 * @since 3.5.1
	 * @deprecated As of release 6.0.0, replaced with {@link #getBrowser}.
	 */
	public static final boolean isGecko(String userAgent) {
		if (userAgent == null)
			return false;

		userAgent = userAgent.toLowerCase(java.util.Locale.ENGLISH);
		return userAgent.indexOf("gecko/") >= 0 && userAgent.indexOf("safari") < 0;
	}

	/**
	 * Returns whether the browser is Gecko 3 based, such as Firefox 3.
	 * 
	 * @since 3.5.0
	 * @deprecated As of release 6.0.0, replaced with {@link #getBrowser}.
	 */
	public static final boolean isGecko3(ServletRequest req) {
		return (req instanceof HttpServletRequest) && isGecko3(getUserAgent(req));
	}

	/**
	 * Returns whether the browser is Gecko 3 based, such as Firefox 3.
	 *
	 * @param userAgent
	 *            represents a client. For HTTP clients, It is the user-agent
	 *            header.
	 * @since 3.5.1
	 * @deprecated As of release 6.0.0, replaced with {@link #getBrowser}.
	 */
	public static final boolean isGecko3(String userAgent) {
		return isBrowser(userAgent, "gecko3");
	}

	/**
	 * Returns whether the browser is Safari.
	 * 
	 * @deprecated As of release 6.0.0, replaced with {@link #getBrowser}.
	 */
	public static final boolean isSafari(ServletRequest req) {
		return (req instanceof HttpServletRequest) && isSafari(getUserAgent(req));
	}

	/**
	 * Returns whether the browser is Safari.
	 *
	 * @param userAgent
	 *            represents a client. For HTTP clients, It is the user-agent
	 *            header.
	 * @since 3.5.1
	 * @deprecated As of release 6.0.0, replaced with {@link #getBrowser}.
	 */
	public static final boolean isSafari(String userAgent) {
		if (userAgent == null)
			return false;

		userAgent = userAgent.toLowerCase(java.util.Locale.ENGLISH);
		return userAgent.indexOf("safari") >= 0;
	}

	/**
	 * Returns whether the browser is Opera.
	 * 
	 * @deprecated As of release 6.0.0, replaced with {@link #getBrowser}.
	 */
	public static final boolean isOpera(ServletRequest req) {
		return (req instanceof HttpServletRequest) && isOpera(getUserAgent(req));
	}

	/**
	 * Returns whether the browser is Opera.
	 *
	 * @param userAgent
	 *            represents a client. For HTTP clients, It is the user-agent
	 *            header.
	 * @since 3.5.1
	 * @deprecated As of release 6.0.0, replaced with {@link #getBrowser}.
	 */
	public static final boolean isOpera(String userAgent) {
		if (userAgent == null)
			return false;

		userAgent = userAgent.toLowerCase(java.util.Locale.ENGLISH);
		return userAgent.indexOf("opera") >= 0;
	}

	/**
	 * Returns whether the client is a mobile device supporting HIL (Handset
	 * Interactive Language). For example, ZK Mobile for Android.
	 *
	 * @since 3.0.2
	 * @deprecated As of release 6.0.0, replaced with {@link #getBrowser}.
	 */
	public static final boolean isHilDevice(ServletRequest req) {
		return (req instanceof HttpServletRequest) && isHilDevice(getUserAgent(req));
	}

	/**
	 * Returns whether the client is a mobile device supporting HIL (Handset
	 * Interactive Language). For example, ZK Mobile for Android.
	 *
	 * @param userAgent
	 *            represents a client. For HTTP clients, It is the user-agent
	 *            header.
	 * @since 3.5.1
	 * @deprecated As of release 6.0.0, replaced with {@link #getBrowser}.
	 */
	public static final boolean isHilDevice(String userAgent) {
		if (userAgent == null)
			return false;

		// ZK Mobile for Android 1.0 (RMIL; RHIL)
		userAgent = userAgent.toLowerCase(java.util.Locale.ENGLISH);
		return userAgent.indexOf("zk") >= 0 && userAgent.indexOf("rhil") >= 0;
	}

	/**
	 * Returns the IE compatibility information.
	 * 
	 * @param request
	 * @return three double values in array [ie version, trident version, ie real
	 *         version]
	 * @since 6.5.5
	 */
	public static double[] getIECompatibilityInfo(ServletRequest request) {
		final String s = ((HttpServletRequest) request).getHeader("user-agent");
		if (isBrowser(s, "ie")) {
			final String ua = s.toLowerCase(Locale.ENGLISH);
			Matcher tridentMatcher = _rtrident.matcher(ua);
			if (tridentMatcher.find()) {
				double tridentVersion = Double.parseDouble(tridentMatcher.group(1));
				Matcher msie = _rmsie.matcher(ua);
				Matcher ie = _rmozilla.matcher(ua); // ie11~
				Matcher ieMatcher = msie.matches() ? msie : (ie.matches() ? ie : null);
				if (ieMatcher != null) {
					double ieVersion = getVersion(ieMatcher);
					double ieVersionReal = tridentVersion + 4.0;
					return new double[] { ieVersion, tridentVersion, ieVersionReal };
				}
			}
		}
		return null;
	}

	/**
	 * Returns the user-agent header, which indicates what the client is, or an
	 * empty string if not available.
	 *
	 * <p>
	 * Note: it doesn't return null, so it is easy to test what the client is with
	 * {@link String#indexOf}.
	 *
	 * @since 3.0.2
	 */
	public static final String getUserAgent(ServletRequest req) {
		if (req instanceof HttpServletRequest) {
			final String s = ((HttpServletRequest) req).getHeader("user-agent");
			if (s != null) {
				double[] ieCompatibilityInfo = getIECompatibilityInfo(req);
				if (ieCompatibilityInfo != null) {
					return s + "; MSIE " + ieCompatibilityInfo[2];
				}
				return s;
			}
		}
		return "";
	}

	/**
	 * Returns the request detail infomation. It is used to log the debug info.
	 * 
	 * @since 3.0.5
	 */
	public static String getDetail(ServletRequest request) {
		final HttpServletRequest hreq = request instanceof HttpServletRequest ? (HttpServletRequest) request : null;
		final StringBuffer sb = new StringBuffer(128);
		if (hreq != null) {
			sb.append(" sid: ").append(hreq.getHeader("ZK-SID")).append('\n');
			addHeaderInfo(sb, hreq, "user-agent");
			addHeaderInfo(sb, hreq, "content-length");
			addHeaderInfo(sb, hreq, "content-type");
			// sb.append(" method: ").append(hreq.getMethod());
		}
		sb.append(" ip: ").append(request.getRemoteAddr());
		return sb.toString();
	}

	private static void addHeaderInfo(StringBuffer sb, HttpServletRequest request, String header) {
		sb.append(' ').append(header).append(": ").append(request.getHeader(header)).append('\n');
	}

	/**
	 * Returns the normal path; that is, will elminate the double dots ".."(parent)
	 * and single dot "."(current) in the path as possible. e.g. /abc/../def would
	 * be normalized to /def; /abc/./def would be normalized to /abc/def; /abc//def
	 * would be normalized to /abc/def.
	 * <p>
	 * Note that if found no way to navigate the path, it is deemed as an illegal
	 * path. e.g. /../abc or /../../abc is deemed as illegal path since we don't
	 * know how to continue doing the normalize.
	 * 
	 * @since 3.6.2
	 */
	public static String getNormalPath(String path) {
		final StringBuffer sb = new StringBuffer(path);
		final IntStack slashes = new IntStack(32); // most 32 slash in a path
		slashes.push(-1);
		int j = 0, colon = -100, dot1 = -100, dot2 = -100;
		for (; j < sb.length(); ++j) {
			final char c = sb.charAt(j);
			switch (c) {
			case '/':
				if (dot1 >= 0) { // single dot or double dots
					if (dot2 >= 0) { // double dots
						int preslash = slashes.pop();
						if (preslash == 0) { // special case "/../"
							throw new IllegalArgumentException("Illegal path: " + path);
						}
						if (slashes.isEmpty()) {
							slashes.push(-1);
						}
						dot2 = -100;
					}
					int b = slashes.peek();
					sb.delete(b + 1, j + 1);
					j = b;
					dot1 = -100;
				} else { // no dot
					int s = slashes.peek();
					if (s >= 0) {
						if (j == (s + 1)) { // consequtive slashs
							if (colon == (s - 1)) { // e.g. "http://abc"
								slashes.clear();
								slashes.push(-1);
								slashes.push(j);
							} else {
								--j;
								sb.delete(j, j + 1);
							}
							continue;
						}
					}
					slashes.push(j);
				}
				break;
			case '.':
				if (dot1 < 0) {
					if (slashes.peek() == (j - 1))
						dot1 = j;
				} else if (dot2 < 0) {
					dot2 = j;
				} else { // more than 2 consecutive dots
					throw new IllegalArgumentException("Illegal path: " + path);
				}
				break;
			case ':':
				if (colon >= 0) {
					throw new IllegalArgumentException("Illegal path: " + path);
				}
				colon = j;
			default:
				dot1 = dot2 = -100;
			}
		}
		return sb.toString();
	}

	private static class IntStack {
		private int _top = -1;
		private int[] _value;

		public IntStack(int sz) {
			_value = new int[sz];
		}

		public boolean isEmpty() {
			return _top < 0;
		}

		public int peek() {
			return _top >= 0 && _top < _value.length ? _value[_top] : -100;
		}

		public int pop() {
			return _value[_top--];
		}

		public void push(int val) {
			_value[++_top] = val;
		}

		public void clear() {
			_top = -1;
		}
	}
}
