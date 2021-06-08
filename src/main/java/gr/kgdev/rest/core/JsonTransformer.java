package gr.kgdev.rest.core;

import java.util.Collection;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import spark.ResponseTransformer;

public class JsonTransformer implements ResponseTransformer {

	@Override
	public String render(Object object) throws Exception {
		if (object instanceof Collection<?>)
			return new JSONArray((Collection<?>) object).toString();
		else if (object instanceof Map<?, ?>)
			return new JSONObject((Map<?,?>) object).toString();
		else if (object instanceof String)
			return new JSONObject((String) object).toString();
		else
			return new JSONObject(object).toString();
	}

}
