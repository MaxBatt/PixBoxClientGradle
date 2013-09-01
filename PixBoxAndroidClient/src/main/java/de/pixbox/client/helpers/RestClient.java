package de.pixbox.client.helpers;

import org.apache.http.client.params.ClientPNames;

import com.loopj.android.http.*;

/**
 * Asynchronous HTTP Client based on AsyncHTTPClient (see http://loopj.com/android-async-http/)</br>
 * This is a static class for easy using HTTP Requests</br>
 * 
 * @author Max Batt
 *
 */
public class RestClient {
  private static final String BASE_URL = "http://www.maxbatt.de/picbox/";

  private static AsyncHttpClient client = new AsyncHttpClient();

  /**
   * GET-Request
 * @param relativeURL
 * @param params
 * @param responseHandler
 */
public static void get(String relativeURL, RequestParams params, AsyncHttpResponseHandler responseHandler) {
	  //System.out.println(getAbsoluteUrl(relativeURL));
      client.get(getAbsoluteUrl(relativeURL), params, responseHandler);
      client.getHttpClient().getParams().setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, true);
  }


  /**
   * POST-Request
 * @param relativeURL
 * @param params
 * @param responseHandler
 */
public static void post(String relativeURL, RequestParams params, AsyncHttpResponseHandler responseHandler) {
      client.post(getAbsoluteUrl(relativeURL), params, responseHandler);
      client.getHttpClient().getParams().setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, true);
  }
  

  /**
   * Construct final URL for request
 * @param relativeUrl
 * @return
 */
private static String getAbsoluteUrl(String relativeUrl) {
      return BASE_URL + relativeUrl;
  }
}
