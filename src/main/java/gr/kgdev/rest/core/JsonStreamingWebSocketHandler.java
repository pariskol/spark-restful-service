package gr.kgdev.rest.core;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

@WebSocket
public class JsonStreamingWebSocketHandler<T> implements WebSocketHandler {

	private Map<String, Session> sessionsMap = new HashMap<>();
	
	@Override
	@OnWebSocketConnect
    public void onConnect(Session user) throws Exception {
		sessionsMap.put(user.getRemoteAddress().toString(), user);
    }

	@Override
	@OnWebSocketClose
    public void onClose(Session user, int statusCode, String reason) {
        sessionsMap.remove(user.getRemoteAddress().toString());
    }

	public void sendMessage(T obj) {
		sessionsMap.values()
		   .stream()
		   .filter(Session::isOpen)
		   .forEach(session -> {
		            try {
						session.getRemote().sendString(new JSONObject(obj).toString());
					} catch (IOException e) {
						LoggerFactory.getLogger("spark").error("Error in websocket", e);
					}
		   });
	}
	
	
	@Override
	@OnWebSocketMessage
    public void onMessage(Session user, String message) {

    }

}
