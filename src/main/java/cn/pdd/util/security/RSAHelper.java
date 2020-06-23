/**
 * 
 */
package cn.pdd.util.security;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;

import org.apache.commons.codec.binary.Base64;

/**
 * rsa非对称加密;
 * @author paddingdun
 *
 * 2018年7月3日
 * @since 1.0
 * @version 1.0
 */
public class RSAHelper {
	
	private final static String ALGORITHM_SHA = "RSA";				//160bit
	
	/**
	 * 生成密钥;
	 * @param size
	 * @return
	 * @throws Exception
	 */
	public static String[] generateKey(int size)throws Exception {
		KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance(ALGORITHM_SHA);
		keyGenerator.initialize(size);
		KeyPair keyPair = keyGenerator.genKeyPair();
		PrivateKey privateKey = keyPair.getPrivate();
		PublicKey publicKey =keyPair.getPublic();
		
		String priStr = Base64.encodeBase64String(privateKey.getEncoded());
		String pubStr = Base64.encodeBase64String(publicKey.getEncoded());
		
		return new String[] {priStr, pubStr};
	}
	
	public static void main(String[] args) throws Exception {
	}
}
