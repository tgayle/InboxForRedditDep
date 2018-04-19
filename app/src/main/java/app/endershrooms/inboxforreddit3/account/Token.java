package app.endershrooms.inboxforreddit3.account;

import java.io.Serializable;

/**
 * Created by Travis on 3/28/2018.
 */

public abstract class Token implements Serializable {
  private String token;

  public String getToken() {
    return token;
  }

  protected void setToken(String token) {
    this.token = token;
  }

  Token(String token) {
    this.token = token;
  }

  @Override
  public String toString() {
    return token;
  }

  public static class AccessToken extends Token{
    long expiresIn; //in seconds. unix timestamp

    public AccessToken(String token, long expiresIn) {
      super(token); //convert to millis.
      this.expiresIn = expiresIn ;
    }

    public boolean isTokenExpired() {
      long currentTime = System.currentTimeMillis();
      boolean isExpire = currentTime > currentTime + expiresIn * 1000;
//      Log.v("Token", "CurrentTime is " + currentTime +
//          ". Expire time is " + (expiresIn * 1000 + System.currentTimeMillis()) + ". Comparison is " +
//          currentTime + " > " + (currentTime + expiresIn * 1000) + " == " + isExpire);
      return isExpire;
    }

    public long getExpiresIn() {
      return expiresIn;
    }
  }

  public static class RefreshToken extends Token {
    public RefreshToken(String token) {
      super(token);
    }
}


}