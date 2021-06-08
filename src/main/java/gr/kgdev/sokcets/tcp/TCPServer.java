package gr.kgdev.sokcets.tcp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TCPServer {

	private ServerSocket serverSocket;
	private List<Thread> clientThreads;
	private OnMessageRunnable handler;
	private Logger logger;
	private Thread internalThread = null;

	public static final String OK_RESPONSE = "OK";
	
	public TCPServer(Integer port, OnMessageRunnable handler) throws IOException {
		this.handler = handler;
		this.clientThreads = new ArrayList<>();
		this.serverSocket = new ServerSocket(port);
		this.serverSocket.setSoTimeout(0);
		this.logger = LoggerFactory.getLogger("spark");
	}

	public void start() {
		internalThread = new Thread(() -> {
			logger.debug(this.getClass().getSimpleName() + " has started");
			logger.debug("Listening on port : " + serverSocket.getLocalPort());
			
			while (!Thread.currentThread().isInterrupted()) {
				try {
					
					Socket client = serverSocket.accept();
					String threadName = TCPClientService.class.getSimpleName() + "-thread-" + clientThreads.size() + 1;
					Thread clientThread = new Thread(new TCPClientService(client, handler), threadName);
					clientThread.setDaemon(true);
					clientThreads.add(clientThread);
					clientThread.start();

				} catch (SocketTimeoutException e) {
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				}
			}
			
			for (Thread th : clientThreads)
				th.interrupt();

			clientThreads.clear();

			SocketUtils.closeSocketSafely(serverSocket);
		}, this.getClass().getSimpleName() + "-thread");
		
		internalThread.start();
	}
	
	public ServerSocket getServerSocket() {
		return serverSocket;
	}

	

}
