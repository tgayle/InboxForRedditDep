package app.endershrooms.inboxforreddit3;

import android.util.Log;
import app.endershrooms.inboxforreddit3.account.Authentication;
import app.endershrooms.inboxforreddit3.account.Token.AccessToken;
import app.endershrooms.inboxforreddit3.interfaces.OnCompleteInterface;
import app.endershrooms.inboxforreddit3.models.RedditAccount;
import app.endershrooms.inboxforreddit3.net.model.MessagesJSONResponse;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Travis on 3/27/2018.
 */

public class APIManager {

  /**
   * Downloads messages starting from the newest message in the database
   */


  enum Requests {
    GETMESSAGE,
    GETME
  }

  private static APIManager apiManager;

  public static synchronized APIManager get() {
    if (apiManager == null) {
      apiManager = new APIManager();
    }
    return apiManager;
  }

  public void updateUserToken(RedditAccount user, OnCompleteInterface listener) {
    if (user.getAccessToken() != null && !user.getAccessToken().isTokenExpired()) {
      Log.v("Token work", "No need to update token for " +user.getUsername());
      Log.v("Token noupdate", user.getAccessToken().getExpiresWhen() + " " + user.getUsername());
      listener.onComplete();
      return;
    }

    Singleton.get().getRedditApiOauth().getAccessTokenFromCode(Authentication.authorizationHeader, new Authentication.Params.RefreshParams(user.getRefreshToken()))
        .observeOn(Schedulers.io())
        .subscribe(jsonLoginResponse -> {
          Log.v("Token work", "Updated token. New results are " + jsonLoginResponse.access_token + " and " + jsonLoginResponse.expires_in);
          user.setAccessToken(new AccessToken(jsonLoginResponse.access_token, jsonLoginResponse.expires_in));
          Singleton.get().getDb().accounts().updateAccount(user);
          listener.onComplete();
        });
//    Log.v("Token work", "Updated token for " + user.getUsername());
  }

  public void downloadMessages(RedditAccount user, String where, int limit, String beforeOrAfter, String beforeAfter, OnCompleteMessageLoad onCompleteInterface) {
    Observable<MessagesJSONResponse> messageLoader = beforeOrAfter.equals("before") ?
        Singleton.get().getRedditApiOauth().getMessagesWithBefore(user.getAuthentication(), where, limit, beforeAfter) :
        Singleton.get().getRedditApiOauth().getMessagesWithAfter(user.getAuthentication(), where, limit, beforeAfter);

    messageLoader
        .observeOn(Schedulers.io())
        .subscribe(messagesJSONResponse -> {
          Singleton.get().getDb().messages().insertMessages(messagesJSONResponse.otherConvertJsonToMessages(user));
//          System.out.println("After was " + messagesJSONResponse.data.after);
          System.out.println("Download messages called with  " +beforeOrAfter + " " + beforeAfter);
          onCompleteInterface.onComplete(beforeOrAfter, messagesJSONResponse.data.after, messagesJSONResponse.data.children.size());
        });
  }

  public void downloadAllPastMessages(RedditAccount user, String where, int limit, String after, OnCompleteMessageLoad onCompleteInterface) {
    BeforeAfterMessageHolder mostRecentAfter = new BeforeAfterMessageHolder(after);
    AtomicInteger mostRecentNumLoaded = new AtomicInteger();

    downloadMessages(user, where, limit, "after", after, (beforeOrAfter, newAfter, messagesLoaded) -> {
      if (after != null) {
        downloadAllPastMessages(user, where, limit, newAfter, onCompleteInterface);
        System.out.println("Downloading all past messages recursed. Count is " + (messagesLoaded) + " and newest after loaded is " + newAfter);
        mostRecentAfter.setCurrent(newAfter);
        mostRecentNumLoaded.set(messagesLoaded);
      } else {
        onCompleteInterface.onComplete(beforeOrAfter, mostRecentAfter.current, mostRecentNumLoaded.get());
      }
    });
  }

  public void downloadAllFutureMessages(RedditAccount user, String where, int limit, String before, OnCompleteMessageLoad onCompleteMessageLoad) {
    BeforeAfterMessageHolder mostRecentBefore = new BeforeAfterMessageHolder(before);
    AtomicInteger mostRecentNumLoaded = new AtomicInteger();

    downloadMessages(user, where, limit, "before", before, (beforeOrAfter, newBefore, messagesLoaded) -> {
      if (before != null) {
        downloadAllFutureMessages(user, where, limit, newBefore, onCompleteMessageLoad);
        System.out.println("Downloading all past messages recursed. Count is " + (messagesLoaded) + " and newest " + beforeOrAfter + " loaded is " + newBefore);
        mostRecentBefore.setCurrent(newBefore);
        mostRecentNumLoaded.set(messagesLoaded);
      } else {
        onCompleteMessageLoad.onComplete("before", mostRecentBefore.current, mostRecentNumLoaded.get());
      }
    });
  }

//  public void downloadNewestMessages(RedditAccount user, String where, int limit, String before, OnCompleteMessageLoad onCompleteInterface) {
//    downloadAllFutureMessages(user, where, limit, before, onCompleteInterface);
//  }

  public interface OnCompleteMessageLoad {
    void onComplete(String beforeOrAfter, String after, int messagesLoaded);
  }

  private static class BeforeAfterMessageHolder {
    private String current;

    public BeforeAfterMessageHolder(String currentAfter) {
      this.current = currentAfter;
      Log.v("BeforeAfterMessageHold", "Created with " + currentAfter);
    }

    public void setCurrent(String currentAfter) {
      this.current = currentAfter;
    }

    public String getCurrent() {
      return current;
    }
  }

}
