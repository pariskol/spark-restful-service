package gr.kgdev.rest.core;

public interface EmbeddedWebSiteService {

	public default void declareRootPath(SparkRESTfulService service) {
		service.get("/", (request, response) -> {
			response.redirect("index.html");
			return null;
		});
	}
}
