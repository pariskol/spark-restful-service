package gr.kgdev.sokcets.tcp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketUtils {

	public static void closeSocketSafely(ServerSocket socket) {
		try {
			socket.close();
		} catch (IOException e) {
		}
	}
	
	public static void closeSocketSafely(Socket socket) {
		try {
			socket.close();
		} catch (IOException e) {
		}
	}
}
