package app.endershrooms.inboxforreddit3.models;

import android.annotation.SuppressLint;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;
import java.io.Serializable;

/**
 * Created by Travis on 3/20/2018.
 */

@Entity(tableName = "accounts")
public class RedditAccount implements Serializable {

  @PrimaryKey @NonNull
  private String username;
  private String accessToken;
  private String refreshToken;
  private long   tokenExpirationDate;

  public RedditAccount(String username, String accessToken, String refreshToken, long tokenExpirationDate) {
    this.username = username;
    this.accessToken = accessToken;
    this.refreshToken = refreshToken;
    this.tokenExpirationDate = tokenExpirationDate;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getAccessToken() {
    return accessToken;
  }

  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }

  public String getRefreshToken() {
    return refreshToken;
  }

  public void setRefreshToken(String refreshToken) {
    this.refreshToken = refreshToken;
  }

  public long getTokenExpirationDate() {
    return tokenExpirationDate;
  }

  public void setTokenExpirationDate(long tokenExpirationDate) {
    this.tokenExpirationDate = tokenExpirationDate;
  }

  public String getAuthentication() {
    return "  bearer " + accessToken;
  }

  public static String getAuthentication(String accessToken) {
    return " bearer " + accessToken;
  }

  @SuppressLint("DefaultLocale")
  public String loggingInfo() {
    String formatted = "Username: %s %n"
        + "AToken: %s %n"
        + "RToken: %s %n"
        + "Expires: %d %n";

    return String.format(formatted, username, accessToken, refreshToken, tokenExpirationDate);
  }
}
