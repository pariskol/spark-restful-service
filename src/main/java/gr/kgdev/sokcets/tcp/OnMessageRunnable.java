package gr.kgdev.sokcets.tcp;

import java.io.DataInputStream;
import java.io.DataOutputStream;

@FunctionalInterface
public interface OnMessageRunnable {

	public void onMessageReceived(String ip, String message, DataInputStream in, DataOutputStream out) throws Exception;
}
