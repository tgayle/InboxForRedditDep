package app.endershrooms.inboxforreddit3.models;

import android.annotation.SuppressLint;
import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;
import app.endershrooms.inboxforreddit3.account.Token.AccessToken;
import app.endershrooms.inboxforreddit3.account.Token.RefreshToken;
import java.io.Serializable;

/**
 * Created by Travis on 3/20/2018.
 */

@Entity(tableName = "accounts")
public class RedditAccount implements Serializable {

  @PrimaryKey @NonNull
  private String username;

  @Embedded(prefix = "access_token_")
  private AccessToken accessToken;

  @Embedded(prefix = "refresh_token_")
  private RefreshToken refreshToken;

  public RedditAccount(String username, AccessToken accessToken, RefreshToken refreshToken) {
    this.username = username;
    this.accessToken = accessToken;
    this.refreshToken = refreshToken;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public AccessToken getAccessToken() {
    return accessToken;
  }

  public void setAccessToken(AccessToken accessToken) {
    this.accessToken = accessToken;
  }

  public RefreshToken getRefreshToken() {
    return refreshToken;
  }

  public void setRefreshToken(RefreshToken refreshToken) {
    this.refreshToken = refreshToken;
  }

  public String getAuthentication() {
    return "  bearer " + accessToken;
  }

  public static String getAuthentication(String accessToken) {
    return " bearer " + accessToken;
  }

  @Override
  public String toString() {
    return username;
  }

  @SuppressLint("DefaultLocale")
  public String loggingInfo() {
    String formatted = "Username: %s %n"
        + "AToken: %s %n"
        + "RToken: %s %n"
        + "Expires: %d %n";

    return String.format(formatted, username, accessToken, refreshToken, accessToken.getExpiresWhen());
  }
}
