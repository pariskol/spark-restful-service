package gr.kgdev.sokcets.tcp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.net.SocketException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spark.utils.StringUtils;

public class TCPClientService implements Runnable {

	private Socket socket;
	private Logger logger;
	private OnMessageRunnable handler;

	public TCPClientService(Socket server, OnMessageRunnable handler) {
		this.logger = LoggerFactory.getLogger("spark");
		this.socket = server;
		this.handler = handler;
		try {
			this.socket.setSoTimeout(0);
		} catch (SocketException e) {
			logger.error(e.getMessage(), e);
		}
	}

	public void run() {
		String ip = socket.getRemoteSocketAddress().toString();
		logger.debug("TCP connection established with ip : " + ip);
		try {
			DataInputStream in = new DataInputStream(socket.getInputStream());
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			
			String message = null;
			while (!Thread.currentThread().isInterrupted()) {
				try {
					message = in.readUTF();
					handler.onMessageReceived(ip, message, in, out);

				} catch (Exception e) {
					String msg = !StringUtils.isEmpty(e.getMessage()) ? e.getMessage() : "Socket read failed!";
					logger.error("Connection with ip : " + ip + "has been lost, cause: " + msg);
					break;
				}
			}

			socket.close();

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	
}
