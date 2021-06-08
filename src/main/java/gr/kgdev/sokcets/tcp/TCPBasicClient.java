package gr.kgdev.sokcets.tcp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import org.json.JSONObject;

public class TCPBasicClient {
	
    private Socket clientSocket;
    protected DataOutputStream out;
    private DataInputStream in;
    private boolean jsonStreamIsOpen = false;
	private String ip;
	private int port;

	//example
    public static void main(String[] args) throws Exception {
    	TCPBasicClient cl =new TCPBasicClient();
		cl.connect("localhost", 1886);
		cl.send("test");
		cl.send("test", msg -> System.out.println("Cool"));
		cl.openJSONStream("DEFAULT", json -> {
			System.out.println(json.toString());
		});
		
		Thread.sleep(1000);
		cl.send("test", msg -> System.out.println("Cool"));
		Thread.sleep(10000);
		cl.send("test", msg -> System.out.println("Cool"));
		
		cl.disconnect();
    }
    
    public void openJSONStream(String topic, JSONHandler handler) throws IOException {
    	jsonStreamIsOpen = true;
    	Thread t = new Thread(() -> {
    		try {
    			TCPBasicClient internalClient = new TCPBasicClient();
    			internalClient.connect(ip, port);
    			
    	    	internalClient.getOutputStream().writeUTF(TCPJSONStreamer.JSON_STREAM);
    	    	internalClient.getOutputStream().writeUTF(topic);
	    		while(jsonStreamIsOpen) {
	    			String rcv = internalClient.getInputStream().readUTF();
	    			handler.onReceive(new JSONObject(rcv));
	    		}
    		} catch (Exception e) {
				e.printStackTrace();
			}
    	});
    	t.setDaemon(true);
    	t.start();
	}
    
    public void closeJSONStream() {
    	jsonStreamIsOpen = false;
    }

	public void connect(String ip, int port) throws UnknownHostException, IOException {
        clientSocket = new Socket(ip, port);
        this.ip = ip;
        this.port = port;
		in = new DataInputStream(clientSocket.getInputStream());
		out = new DataOutputStream(clientSocket.getOutputStream());
    }

    public void send(String msg, MessageHandler handler) throws IOException {
        out.writeUTF(msg);
        String resp = in.readUTF();
        handler.onReceive(resp);
    }
    
    public void send(String msg) throws IOException {
        out.writeUTF(msg);
    }

    public void sendFile(String filePath) throws Exception {
    	send(TCPFiletransferer.FILE_TRANSFER);
		TCPFiletransferer.sendFile(filePath, this.out);
    }
    
    public void disconnect() throws IOException {
        in.close();
        out.close();
        closeJSONStream();
        clientSocket.close();
    }

    public Socket getSocket() {
    	return clientSocket;
    }
    
	public DataOutputStream getOutputStream() {
		return out;
	}

	public DataInputStream getInputStream() {
		return in;
	}
    
    
}
