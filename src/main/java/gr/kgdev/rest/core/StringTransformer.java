package gr.kgdev.rest.core;

import spark.ResponseTransformer;

public class StringTransformer implements ResponseTransformer {

	@Override
	public String render(Object object) throws Exception {
		return object.toString();
	}
}
