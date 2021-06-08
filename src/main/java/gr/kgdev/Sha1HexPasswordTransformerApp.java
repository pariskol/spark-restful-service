package gr.kgdev;

import org.apache.commons.codec.digest.DigestUtils;

public class Sha1HexPasswordTransformerApp {

	public static void main(String[] args) {
		System.out.println(DigestUtils.sha1Hex(args[0]));
	}
}
