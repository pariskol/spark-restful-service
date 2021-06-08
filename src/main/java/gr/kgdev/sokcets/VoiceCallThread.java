package gr.kgdev.sokcets;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VoiceCallThread extends Thread {

    private DatagramSocket socket;
    private byte[] buf = new byte[256];
    private Logger logger = LoggerFactory.getLogger("spark");
	
	@Override
	public void run() {
		try {
	        socket = new DatagramSocket(50000);
			while (!this.isInterrupted()) {
				DatagramPacket packet = new DatagramPacket(buf, buf.length);
				socket.receive(packet);
				// TODO redirect to actual user's inet address
				InetAddress address = packet.getAddress();

				int port = packet.getPort();
				packet = new DatagramPacket(buf, buf.length, address, port);
				socket.send(packet);
			}
			socket.close();
		} catch (IOException e) {
			logger.error(e.getMessage());
			reconnect();
		}
	}

	private void reconnect() {
		try {
			logger.info("Trying to recconect after 5 secs");
			Thread.sleep(5000);
			run();
		} catch (InterruptedException e1) {
		}
	}

}
