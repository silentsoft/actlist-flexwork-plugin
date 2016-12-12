import java.util.function.Consumer;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;

public class RESTfulAPI extends org.silentsoft.net.rest.RESTfulAPI {
	
	public static void init(String uri, String root) {
		org.silentsoft.net.rest.RESTfulAPI.init(uri, root);
	}
	
	public static <T> T doGet(String api, Class<T> returnType, Consumer<HttpRequest> beforeRequest, Consumer<HttpResponse> afterResponse) throws Exception {
		return org.silentsoft.net.rest.RESTfulAPI.doGet(api, returnType, beforeRequest, afterResponse);
	}
	
	public static <T> T doPost(String api, Object param, Class<T> returnType, Consumer<HttpRequest> beforeRequest, Consumer<HttpResponse> afterResponse) throws Exception {
		return org.silentsoft.net.rest.RESTfulAPI.doPost(api, param, returnType, beforeRequest, afterResponse);
	}
	
}
