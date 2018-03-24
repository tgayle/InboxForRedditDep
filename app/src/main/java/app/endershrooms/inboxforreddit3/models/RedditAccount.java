package app.endershrooms.inboxforreddit3.models;

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

  public RedditAccount(String username, String accessToken, String refreshToken) {
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
}
