package app.endershrooms.inboxforreddit3.net;

/**
 * Created by Travis on 3/25/2018.
 */

public class JSONLoginResponse {

  public String access_token;
  public long expires_in;
  public String refresh_token;

  public static long expiresInDate(long expires_in) {
    if (expires_in < 10000) {
      return System.currentTimeMillis() + expires_in * 1000;
    }
    return expires_in;
  }

}
