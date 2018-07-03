/**
 * 
 */
package cn.pdd.util.security;

import java.security.Key;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

/**
 * DES对称加密;
 * @author paddingdun
 *
 * 2018年7月3日
 * @since 1.0
 * @version 1.0
 */
public class DESHelper {
	
	private final static String ALGORITHM = "DES";
	
	private final static String TRANSFORMATION_ECB = "DES/ECB/PKCS5Padding";
	
	private final static String TRANSFORMATION_CBC = "DES/CBC/PKCS5Padding";
	
	/**
	 * 生成密钥;
	 * @param size
	 * @return
	 * @throws Exception
	 */
	public static byte[] generateKey(int size)throws Exception {
		KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
		keyGenerator.init(size);
		SecretKey secretKey = keyGenerator.generateKey();
		byte[] keyBytes = secretKey.getEncoded();
		return keyBytes;
	}
	
	/**
	 * 默认128;
	 * @return
	 * @throws Exception
	 */
	public static byte[] generateKey()throws Exception {
		return generateKey(56);
	}
	
	/**
	 * ECB加密, 同一明文每次加密后密文一样;
	 * @param srcBytes
	 * @param keyBytes
	 * @return
	 * @throws Exception
	 */
	public static byte[] encrypt(byte[] srcBytes, byte[] keyBytes)throws Exception {
		Cipher cipher = Cipher.getInstance(TRANSFORMATION_ECB);
		Key key = new SecretKeySpec(keyBytes, ALGORITHM);
		cipher.init(Cipher.ENCRYPT_MODE, key);
		byte[] encodeResult = cipher.doFinal(srcBytes);
		return encodeResult;
	}
	
	/**
	 * CBC加密, 由于存在iv向量, 可以对同一明文得到不同的密文;
	 * @param srcBytes
	 * @param keyBytes
	 * @param iv
	 * @return
	 * @throws Exception
	 */
	public static byte[] encrypt(byte[] srcBytes, byte[] keyBytes, byte[] iv)throws Exception {
		Cipher cipher = Cipher.getInstance(TRANSFORMATION_CBC);
		Key key = new SecretKeySpec(keyBytes, ALGORITHM);
		IvParameterSpec ivps = new IvParameterSpec(iv);
		cipher.init(Cipher.ENCRYPT_MODE, key, ivps);
		byte[] encodeResult = cipher.doFinal(srcBytes);
		return encodeResult;
	}
	
	/**
	 * ECB解密;
	 * @param encryptBytes
	 * @param keyBytes
	 * @return
	 * @throws Exception
	 */
	public static byte[] decrypt(byte[] encryptBytes, byte[] keyBytes)throws Exception {
		Cipher cipher = Cipher.getInstance(TRANSFORMATION_ECB);
		Key key = new SecretKeySpec(keyBytes, ALGORITHM);
		cipher.init(Cipher.DECRYPT_MODE, key);
		byte[] decodeResult = cipher.doFinal(encryptBytes);
		return decodeResult;
	}
	
	/**
	 * CBC解密;
	 * @param encryptBytes
	 * @param keyBytes
	 * @param iv
	 * @return
	 * @throws Exception
	 */
	public static byte[] decrypt(byte[] encryptBytes, byte[] keyBytes, byte[] iv)throws Exception {
		Cipher cipher = Cipher.getInstance(TRANSFORMATION_CBC);
		Key key = new SecretKeySpec(keyBytes, ALGORITHM);
		IvParameterSpec ivps = new IvParameterSpec(iv);
		cipher.init(Cipher.DECRYPT_MODE, key, ivps);
		byte[] decodeResult = cipher.doFinal(encryptBytes);
		return decodeResult;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args)throws Exception {
		KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
		keyGenerator.init(128, new SecureRandom("123".getBytes("utf-8")));
		SecretKey secretKey = keyGenerator.generateKey();
		byte[] keyBytes = secretKey.getEncoded();
		System.out.println(Base64.encodeBase64String(keyBytes));
		
//		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
//		Key key = new SecretKeySpec(Base64.decodeBase64("q2XLRyWL1RsrhzNK9FkoKg=="), "AES");
//		IvParameterSpec iv = new IvParameterSpec("1234567890123456".getBytes());
//		cipher.init(Cipher.ENCRYPT_MODE, key, iv);
//		String a = Base64.encodeBase64String(cipher.doFinal("我们".getBytes("utf-8")));
//		System.out.println(a);
		//cwDUK8k3syc6kryVtUAAyg== 123
		//ICuaYSs8HjCAOKfH+iYi8Q== abc
		//VjSdtWbUtxllAFNk1e0Wqw== abc
		//QBz+VqA2Wgf+Az2+RA6+mQ==
		
		//QBz+VqA2Wgf+Az2+RA6+mQ==
		//IPNUhArru+Mz6pJ31aVN/A==
//		String s = "1234567890123456";
//		String s = "1234567890123458";
//		String a = Base64.encodeBase64String(encrypt("我们".getBytes(), Base64.decodeBase64("q2XLRyWL1RsrhzNK9FkoKg=="), s.getBytes()));
//		System.out.println(a);
		
//		byte[] a = decrypt(Base64.decodeBase64("IPNUhArru+Mz6pJ31aVN/A=="), Base64.decodeBase64("q2XLRyWL1RsrhzNK9FkoKg=="), s.getBytes());
//		System.out.println(new String(a, "utf-8"));
		
//		String key = Base64.encodeBase64String(generateKey(56));
//		System.out.println(key);
		
//		String key = "4LMcAnomNIk=";
	}

}
