package gr.kgdev.sokcets.tcp;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TCPJSONStreamer {

	private static final String DEFAULT_TOPIC = "DEFAULT";
	private static final Map<String, DataOutputStream> registeredStreams = new HashMap<>();
	private static final Map<String, List<String>> topics = new HashMap<>();
	
	public static final String JSON_STREAM = "JSON_STREAM";
	private static final Logger LOGGER = LoggerFactory.getLogger("spark");
	
	
	static {
		createTopic(DEFAULT_TOPIC);
	}
	
	public synchronized static void stream(JSONObject json) {
		stream(DEFAULT_TOPIC, json);
	}
	
	public synchronized static void stream(String topic, JSONObject json) {
		List<String> closedStreams = new ArrayList<>();
		topics.get(topic).parallelStream().forEach(ip -> {
			DataOutputStream stream = registeredStreams.get(ip);
			try {
				stream.writeUTF(json.toString());
			} catch (IOException e) {
				closedStreams.add(ip);
			}
		});
		closedStreams.forEach(ip -> {
			registeredStreams.remove(ip);
			topics.get(topic).remove(ip);
			LOGGER.error("Streaming to IP : " + ip + " ,has been closed");
		});
	}
	
	public synchronized static void register(String ip, DataOutputStream stream) {
		register(DEFAULT_TOPIC, ip, stream);
	}
	
	public synchronized static void register(String topic, String ip, DataOutputStream stream) {
		registeredStreams.put(ip, stream);
		topics.get(topic).add(ip);
		LOGGER.info("IP : " + ip + " , has been registered for streaming to topic : " + topic);
	}
	
	public synchronized static void createTopic(String name) {
		topics.put(name, new ArrayList<>());
		LOGGER.info("Topic  '" + name+ "'  has been created");
	}
}
