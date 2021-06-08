package gr.kgdev;

import gr.kgdev.sokcets.VoiceCallThread;

public class SocketTestApp {

	public static void main(String[] args) throws InterruptedException {
		
		VoiceCallThread vt = new VoiceCallThread();
		vt.start();
		vt.join();
	}
}
