package com.ibm.cognos.auth.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.apache.axis.encoding.Base64;

public class EncryptUtil {
	public static String getEncryptedString(String type, String str) {
		String result = "";
		if("MD5".equals(type)) {
			result = getEncryptedMD5(str);
		} else if("SHA256".equals(type)) {
			result = getEncryptedSHA256(str);
		} else if("BASE64".equals(type)) {
			result = getEncryptedBase64(str);
		} else {
			result = str;
		}
		
		return result;
	}
	
	public static String getEncryptedMD5(String str) {
		String result = "";
		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");
			digest.reset();
			digest.update(str.getBytes());
			byte[] hash = digest.digest();
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < hash.length; i++) {
				sb.append(Integer.toString((hash[i]&0xff) + 0x100, 16).substring(1));
				//String hex = Integer.toHexString(0xff & hash[i]);
				//sb.append(hex.length() == 1 ? "0" : "").append(hex);
			}
			result = sb.toString();
		} catch (NoSuchAlgorithmException nsae) {
			result = str;
		}
		return result;
	}
	
	public static String getEncryptedSHA256(String str) {
		String result = "";
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			digest.reset();
			digest.update(str.getBytes());
			byte[] hash = digest.digest();
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < hash.length; i++) {
				sb.append(Integer.toString((hash[i]&0xff) + 0x100, 16).substring(1));
			}
			result = sb.toString();
		} catch (NoSuchAlgorithmException nsae) {
			result = str;
		}
		return result;
	}
	
	public static String getEncryptedBase64(String str) {
		String result = "";
		try {
			result = Base64.encode(str.getBytes());
		} catch(Exception e) {
			result = str;
		}
		return result;
	}
	
	public static String getCredentialPassword() {
		return getEncryptedSHA256("IBMCOGNOS");
	}
	
	public static void main(String[] args) {
		String inputPassword;
		if(args.length > 0) {
			inputPassword = args[0];
		} else {
			inputPassword = "admin";
		}
		String password = EncryptUtil.getEncryptedString("SHA256", inputPassword);
		System.out.println(password);
	}
}
