import java.security.MessageDigest;
import java.util.Arrays;

public class StringUtil {
	public static String getHash(String in) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(in.getBytes("UTF-8"));
			StringBuffer hexString = new StringBuffer();
			for (byte b : hash) {
				hexString.append(String.format("%02x", b));
			}
			return hexString.toString();
		} catch(Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		
	}
}
