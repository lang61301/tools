/**
 * 
 */
package cn.pdd.util.security;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;

import org.apache.commons.lang.Validate;

/**
 * SHA
 * 返回ByteSource，可进一步被编码为Hex, Base64或UrlSafeBase64
 * SHA将输入流(输入报文的长度不限)按照每块512位（64个字节）进行分块，并产生160位(20个字节)的被称为信息认证代码或信息摘要的输出。
 * 			block 	word 	digest(输出位数) 
 * SHA-1 	512 	32 		160 
 * SHA-256 	512 	32 		256 
 * SHA-384 	1024 	64 		384 
 * SHA-512	1024 	64 		512
 * @author paddingdun
 *
 * 2018年7月3日
 * @since 1.0
 * @version 1.0
 */
public class SHAHelper {
	private final static String ALGORITHM_SHA1 	 = "SHA-1";			//160bit
	private final static String ALGORITHM_SHA256 = "SHA-256";		//256bit
	private final static String ALGORITHM_SHA384 = "SHA-384";		//385bit
	private final static String ALGORITHM_SHA512 = "SHA-512";		//512bit
	
	private static SecureRandom random = new SecureRandom();

	/**
	 * 对输入字符串进行sha1散列.
	 */
	public static byte[] sha512(byte[] input) {
		return digest(input, ALGORITHM_SHA512, null, 1);
	}

	public static byte[] sha512(byte[] input, byte[] salt) {
		return digest(input, ALGORITHM_SHA512, salt, 1);
	}

	public static byte[] sha512(byte[] input, byte[] salt, int iterations) {
		return digest(input, ALGORITHM_SHA512, salt, iterations);
	}

	/**
	 * 对字符串进行散列, 支持sha算法.
	 */
	private static byte[] digest(byte[] input, String algorithm, byte[] salt, int iterations) {
		try {
			MessageDigest digest = MessageDigest.getInstance(algorithm);

			if (salt != null) {
				digest.update(salt);
			}

			byte[] result = digest.digest(input);

			for (int i = 1; i < iterations; i++) {
				digest.reset();
				result = digest.digest(result);
			}
			return result;
		} catch (GeneralSecurityException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 生成随机的Byte[]作为salt.
	 * 
	 * @param numBytes
	 *            byte数组的大小
	 */
	public static byte[] generateSalt(int numBytes) {
		Validate.isTrue(numBytes > 0, "numBytes argument must be a positive integer (1 or larger)", numBytes);

		byte[] bytes = new byte[numBytes];
		random.nextBytes(bytes);
		return bytes;
	}

	/**
	 * 对文件进行sha1散列.
	 */
	public static byte[] sha512(InputStream input) throws IOException {
		return digest(input, ALGORITHM_SHA512);
	}

	private static byte[] digest(InputStream input, String algorithm) throws IOException {
		try {
			MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
			int bufferLength = 8 * 1024;
			byte[] buffer = new byte[bufferLength];
			int read = -1;

			while ((read = input.read(buffer, 0, bufferLength)) > -1) {
				messageDigest.update(buffer, 0, read);
			}
			return messageDigest.digest();
		} catch (GeneralSecurityException e) {
			throw new RuntimeException(e);
		}
	}
}
