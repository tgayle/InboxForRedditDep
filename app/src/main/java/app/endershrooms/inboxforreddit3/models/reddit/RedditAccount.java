package app.endershrooms.inboxforreddit3.models.reddit;

import android.annotation.SuppressLint;
import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;
import android.util.Log;
import app.endershrooms.inboxforreddit3.account.Token.AccessToken;
import app.endershrooms.inboxforreddit3.account.Token.RefreshToken;
import app.endershrooms.inboxforreddit3.net.model.JSONLoginResponse;
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

  private boolean accountIsNew;

  public RedditAccount(@NonNull String username, AccessToken accessToken, RefreshToken refreshToken, boolean accountIsNew) {
    this.username = username;
    this.accessToken = accessToken;
    this.refreshToken = refreshToken;
    this.accountIsNew = accountIsNew;
    Log.d("RoomReddit", username + " created from room and account is " + (accountIsNew ? "new":"not new"));
    //FIXME: Accounts get marked as not new too soon?
  }

  @Ignore
  public RedditAccount(@NonNull String username, AccessToken accessToken, RefreshToken refreshToken) {
    this.username = username;
    this.accessToken = accessToken;
    this.refreshToken = refreshToken;
    this.accountIsNew = false;
  }

  @Ignore
  public RedditAccount(@NonNull String username, JSONLoginResponse jsonLoginResponse) {
    this.username = username;
    this.accessToken = new AccessToken(jsonLoginResponse.access_token, jsonLoginResponse.expires_in);
    this.refreshToken = new RefreshToken(jsonLoginResponse.refresh_token);
    this.accountIsNew = true;
    Log.d("AccountCreate", username + " created with " +((accountIsNew) ? "new" : "not new"));
  }

  @NonNull
  public String getUsername() {
    return username;
  }

  public void setUsername(@NonNull String username) {
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

  public boolean getAccountIsNew() {
    return accountIsNew;
  }

  public void setAccountIsNew(boolean accountIsNew) {
    Log.d("Account manage", username + " was set to " + accountIsNew + " from " + this.accountIsNew);
    this.accountIsNew = accountIsNew;
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

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof RedditAccount) {
      RedditAccount otherAcc = (RedditAccount) obj;
      return this.getUsername().equals(otherAcc.getUsername()) &&
          this.getAccessToken().equals(otherAcc.getAccessToken()) &&
          this.getRefreshToken().equals(otherAcc.getRefreshToken());
    }
    return false;
  }
}
