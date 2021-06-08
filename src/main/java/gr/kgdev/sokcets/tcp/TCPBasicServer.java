package gr.kgdev.sokcets.tcp;

import java.io.IOException;

import org.json.JSONObject;

public class TCPBasicServer extends TCPServer {

	public TCPBasicServer(Integer port) throws IOException {
		super(port, (ip, message, in, out) -> {
			switch (message) {
			case TCPFiletransferer.FILE_TRANSFER:
				TCPFiletransferer.receiveFile(in);
				break;
			case TCPJSONStreamer.JSON_STREAM:
				String topic = in.readUTF();
				TCPJSONStreamer.register(topic, ip, out);
				break;
			default:
				out.writeUTF(OK_RESPONSE);
				break;
			}
		});
	}

	// example
	public static void main(String[] args) throws Exception {
		new TCPBasicServer(1886).start();
		while(true) {
			TCPJSONStreamer.stream(new JSONObject("{ 'test' : 'OK'}"));
			Thread.sleep(1000);
		}
	}
}
