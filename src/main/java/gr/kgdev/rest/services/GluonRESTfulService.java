package gr.kgdev.rest.services;

import gr.kgdev.rest.core.EmbeddedWebSiteService;
import gr.kgdev.rest.core.SparkRESTfulService;

public class GluonRESTfulService extends SparkRESTfulService implements EmbeddedWebSiteService {

	@Override
	protected void initStaicFilesRoute() {
		getSparkService().staticFiles.location("/angular");
		getSparkService().staticFiles.header("Cache-Control", "max-age=0");
		getSparkService().staticFiles.expireTime(600); // ten minutes
	}

	@Override
	protected void declareWebSockets() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	protected void declareFilters() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void declareEndpoints() {
		declareRootPath(this);
	}

	public static void main(String[] args) {
		GluonRESTfulService service = new GluonRESTfulService();
		service.configure("localhost", 8081);
		service.initStaicFilesRoute();
		service.init(null);
		service.start();
	}
}
