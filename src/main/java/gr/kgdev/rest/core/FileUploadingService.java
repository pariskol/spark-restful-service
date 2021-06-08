package gr.kgdev.rest.core;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.Collection;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import javax.servlet.http.Part;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gr.kgdev.rest.core.exceptions.BadRequestException;
import gr.kgdev.utils.PropertiesLoader;
import spark.Request;
import spark.Response;

public interface FileUploadingService {

	public default String getUploadLoaction() {
		return (String) PropertiesLoader.getProperty("upload.directory", String.class, "./"); // the directory location where files will be stored
	}
	
	/**
	 * @return the maximum size allowed for uploaded files
	 */
	public default long getMaxfileSize() {
		return 100000000;
	}
	
	/**
	 * @return the maximum size allowed for multipart/form-data requests
	 */
	public default long getMaxRequestSize() {
		return 101000000;
	}
	
	/**
	 * @return the size threshold after which files will be written to disk
	 */
	public default int getFileSizeThreshold() {
		return 1;
	}
	
	// Test : curl -i -X POST -H 'Content-Type: multipart/form-data' -H 'Authorization: Basic YWRtaW46dGVzdA==' -F "file=@test.txt" 192.168.2.5:8080/api/action/upload?messageId=8

	public default String uploadFile(Request request, Response response) throws IOException, ServletException, SQLException, BadRequestException {

		Logger logger = LoggerFactory.getLogger("spark");
		
		String location = getUploadLoaction();
		long maxFileSize = getMaxfileSize();
		long maxRequestSize = getMaxRequestSize();
		int fileSizeThreshold = getFileSizeThreshold();
		
		MultipartConfigElement multipartConfigElement = new MultipartConfigElement(location, maxFileSize,
				maxRequestSize, fileSizeThreshold);
		request.raw().setAttribute("org.eclipse.jetty.multipartConfig", multipartConfigElement);

		Collection<Part> parts = request.raw().getParts();
		for (Part part : parts) {
			logger.debug("Name: " + part.getName());
			logger.debug("Size: " + part.getSize());
			logger.debug("Filename: " + part.getSubmittedFileName());
		}

		String fName = request.raw().getPart("file").getSubmittedFileName();
		logger.info("Uploading file: " + fName);

		Part uploadedFile = request.raw().getPart("file");
		Files.createDirectories(Paths.get(location));
		Path out = Paths.get(location + "/" + fName);
		try (final InputStream in = uploadedFile.getInputStream()) {
			Files.copy(in, out, StandardCopyOption.REPLACE_EXISTING);
			uploadedFile.delete();
		}
		
		return JSONMessages.create("File has been uploaded");
	}
	
	public default Object downloadFile(Request request, Response responce) throws Exception {
		String path = request.params(":file");
	    File file = new File(getUploadLoaction() + "/" + path);
	    responce.raw().setContentType("application/octet-stream");
	    responce.raw().setHeader("Content-Disposition","attachment; filename="+file.getName()+".zip");

        try(BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file));)
        {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = bufferedInputStream.read(buffer)) > 0) {
            	responce.raw().getOutputStream().write(buffer,0,len);
            }
        }

	    return JSONMessages.create("OK");
	}

}
