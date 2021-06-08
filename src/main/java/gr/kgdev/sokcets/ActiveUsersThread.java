//package gr.kgdev.sokcets;
//
//import java.awt.AWTException;
//import java.io.DataInputStream;
//import java.io.EOFException;
//import java.io.IOException;
//import java.net.InetAddress;
//import java.net.ServerSocket;
//import java.net.Socket;
//import java.net.SocketTimeoutException;
//import java.net.URISyntaxException;
//import java.util.HashMap;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import gr.kgdev.utils.PropertiesLoader;
//
//public class ActiveUsersThread extends Thread {
//	
//	private HashMap<Integer, InetAddress> usersMap = new HashMap<>();
//    private Logger logger = LoggerFactory.getLogger("spark");
//	private ServerSocket serverSocket;
//	private String ip = (String) PropertiesLoader.getProperty("user.manager.ip", String.class);
//	private Integer port = (Integer) PropertiesLoader.getProperty("user.manager.port", Integer.class);
//	
//	@Override
//	public void run() {
//		serverSocket = new ServerSocket(port);
//	    serverSocket.setSoTimeout(0);
//		String message = null;
//	      while(true) {
//	         try {
//	            logger.info("Waiting for client on port " + 
//	               serverSocket.getLocalPort() + "...");
//	            Socket server = serverSocket.accept();
//	            
//	            DataInputStream in = new DataInputStream(server.getInputStream());
//		        while(!this.isInterrupted()) {
//		            message = in.readUTF();
//		            if(!message.equals("exit"))
//		            {
//			            try {
////								handleMessage(message);
//						} catch (Exception e) {
//							logger.error(e.getMessage());
//						} 
//		            }
//		        }
//	            
//	         } catch (IOException e) {
//	            e.printStackTrace();
//	            break;
//	         } 
//	      }
//	  }
//
//}
