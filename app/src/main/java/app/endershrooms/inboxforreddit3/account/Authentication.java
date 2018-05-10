package app.endershrooms.inboxforreddit3.account;

import android.util.Base64;
import app.endershrooms.inboxforreddit3.Constants;
import app.endershrooms.inboxforreddit3.account.Token.RefreshToken;
import java.util.HashMap;

/**
 * Created by Travis on 3/25/2018.
 */

public class Authentication{

  public static final String creds = Constants.CLIENT_ID + ":" + "";
  public static final String authorizationHeader = "Basic " + Base64
      .encodeToString(creds.getBytes(), Base64.NO_WRAP);

  public static class Params {
    public static abstract class AuthParams extends HashMap<String, String> {
    }

    public static class NewTokenParams extends AuthParams {
      public NewTokenParams(String redirectCode) {
        this.put("grant_type", "authorization_code");
        this.put("code", redirectCode);
        this.put("redirect_uri", Constants.OAUTH_REDIRECT_URI);
      }
    }

    public static class RefreshParams extends AuthParams {
      public RefreshParams(RefreshToken refreshToken) {
        this.put("grant_type", "refresh_token");
        this.put("refresh_token", refreshToken.toString());
      }
    }

  }

}
