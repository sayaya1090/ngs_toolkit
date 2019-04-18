package net.sayaya.util;

public class ByteToHex {
	public static String getHex(byte[] ba) {
		if (ba == null || ba.length == 0) return null;
		StringBuffer sb = new StringBuffer(ba.length * 2);
		String hexNumber;
		for (int x = 0; x < ba.length; x++) {
			hexNumber = "0" + Integer.toHexString(0xff & ba[x]);
			if(x > 0) sb.append(" ");
			sb.append(hexNumber.substring(hexNumber.length() - 2));
		}
		return sb.toString();
	}
	
}
