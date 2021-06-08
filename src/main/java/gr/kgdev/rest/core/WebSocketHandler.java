package gr.kgdev.rest.core;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

@WebSocket
public interface WebSocketHandler {

    @OnWebSocketConnect
    public void onConnect(Session user) throws Exception;
    

    @OnWebSocketClose
    public void onClose(Session user, int statusCode, String reason);

    @OnWebSocketMessage
    public void onMessage(Session user, String message);

}