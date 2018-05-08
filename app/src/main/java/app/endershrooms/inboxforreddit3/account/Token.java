package app.endershrooms.inboxforreddit3.account;

import android.util.Log;
import java.io.Serializable;

/**
 * Created by Travis on 3/28/2018.
 */

public abstract class Token implements Serializable {
  private String token;

  public String getToken() {
    return token;
  }

  Token(String token) {
    this.token = token;
  }

  @Override
  public String toString() {
    return token;
  }

  public static class AccessToken extends Token {
    long expiresWhen; //in milliseconds
    public AccessToken(String token, long expiresWhen) {
      super(token); //convert to millis.
      if (expiresWhen < 10_000) {
        expiresWhen = System.currentTimeMillis() + (expiresWhen * 1000);
      }
      this.expiresWhen = expiresWhen;
    }

    public boolean isTokenExpired() {
      long currentTime = System.currentTimeMillis();
      boolean isExpire = currentTime > expiresWhen;

      Log.v("Token", "CurrentTime is " + currentTime +
          ". Expire time is " + expiresWhen + ". Comparison is " +
          currentTime + " > " + expiresWhen + " == " + isExpire);
      return isExpire;
    }

    public long getExpiresWhen() {
      return expiresWhen;
    }

    @Override
    public boolean equals(Object obj) {
      return obj instanceof AccessToken &&
          ((AccessToken) obj).expiresWhen == this.expiresWhen &&
          ((AccessToken) obj).getToken().equals(this.getToken());
    }
  }

  public static class RefreshToken extends Token {
    public RefreshToken(String token) {
      super(token);
    }

    @Override
    public boolean equals(Object obj) {
      return obj instanceof RefreshToken &&
          ((RefreshToken) obj).getToken().equals(this.getToken());
    }
  }


}