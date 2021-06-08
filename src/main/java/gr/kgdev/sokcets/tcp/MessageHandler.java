package gr.kgdev.sokcets.tcp;

@FunctionalInterface
public interface MessageHandler {

	public void onReceive(String message);
}
