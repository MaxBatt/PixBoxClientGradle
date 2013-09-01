package de.pixbox.client;

import com.loopj.android.http.*;

public class RestClient {
  private static final String BASE_URL = "http://www.maxbatt.de/picbox/";

  private static AsyncHttpClient client = new AsyncHttpClient();

  public static void get(String relativeURL, RequestParams params, AsyncHttpResponseHandler responseHandler) {
	  //System.out.println(getAbsoluteUrl(relativeURL));
      client.get(getAbsoluteUrl(relativeURL), params, responseHandler);
  }

  public static void post(String relativeURL, RequestParams params, AsyncHttpResponseHandler responseHandler) {
      client.post(getAbsoluteUrl(relativeURL), params, responseHandler);
  }

  private static String getAbsoluteUrl(String relativeUrl) {
      return BASE_URL + relativeUrl;
  }
}
