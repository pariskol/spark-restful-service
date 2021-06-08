package gr.kgdev.sokcets.tcp;

import org.json.JSONObject;

@FunctionalInterface
public interface JSONHandler {

	public void onReceive(JSONObject json);
}
