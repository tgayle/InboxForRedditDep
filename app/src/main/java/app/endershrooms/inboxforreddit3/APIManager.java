package app.endershrooms.inboxforreddit3;

import android.util.Log;
import app.endershrooms.inboxforreddit3.account.Authentication;
import app.endershrooms.inboxforreddit3.account.Token.AccessToken;
import app.endershrooms.inboxforreddit3.interfaces.OnCompleteInterface;
import app.endershrooms.inboxforreddit3.models.RedditAccount;
import io.reactivex.schedulers.Schedulers;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Travis on 3/27/2018.
 */

public class APIManager {

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
      listener.onComplete();
      return;
    }

    Singleton.get().getRedditApiOauth().getAccessTokenFromCode(Authentication.authorizationHeader, new Authentication.Params.RefreshParams(user.getRefreshToken()))
        .observeOn(Schedulers.io())
        .subscribe(jsonLoginResponse -> {
          Log.v("Token work", "Updated token. New results are " + jsonLoginResponse.access_token + " and " + jsonLoginResponse.expires_in);
          user.setAccessToken(new AccessToken(jsonLoginResponse.access_token, jsonLoginResponse.expires_in));
          listener.onComplete();
        });
//    Log.v("Token work", "Updated token for " + user.getUsername());
  }
//
//  public void OldupdateMessagesToDatabase(RedditAccount user, String where, int count, int limit, String after, OnCompleteMessageLoad onFinish) {
//    updateUserToken(user, () -> {
//      Singleton.get().getRedditApiOauth().getMessages(user.getAuthentication(), where, count, limit, after)
//          .observeOn(Schedulers.io())
//          .subscribe(messagesJSONResponse -> {
//            List<Message> messages = messagesJSONResponse.convertJsonToMessages(user);
//            Singleton.get().getDb().messages().insertMessages(messages);
//            System.out.println("Data after was " + messagesJSONResponse.data.after);
//            System.out.println("Message info below for:");
//            System.out.println(
//                "user = [" + user + "], where = [" + where + "], count = [" + count + "], limit = ["
//                    + limit + "], after = [" + after + "], onFinish = [" + onFinish + "]");
//            for (Message message : messages) {
//              System.out.println(message.toString());
//            }
//            onFinish.onComplete(messagesJSONResponse.data.after, messages.size());
//          });
//
//    });
//  }

  public void downloadMessages(RedditAccount user, String where, int count, int limit, String after, OnCompleteMessageLoad onCompleteInterface) {
    Singleton.get().getRedditApiOauth().getMessages(user.getAuthentication(), where, count, limit, after)
        .observeOn(Schedulers.io())
        .subscribe(messagesJSONResponse -> {
          Singleton.get().getDb().messages().insertMessages(messagesJSONResponse.otherConvertJsonToMessages(user));
//          System.out.println("After was " + messagesJSONResponse.data.after);
          onCompleteInterface.onComplete(messagesJSONResponse.data.after, messagesJSONResponse.data.children.size());
        });
  }

  public void downloadMessagesStaggered(RedditAccount user, String where, int count, int limit, String after, int totalMessagesToLoad, OnCompleteMessageLoad onCompleteInterface) {
    AfterMessageHolder mostRecentAfter = new AfterMessageHolder(after);
    AtomicInteger mostRecentNumLoaded = new AtomicInteger();

    downloadMessages(user, where, count, limit, after, (newAfter, messagesLoaded) -> {
      if (count < totalMessagesToLoad) {
        downloadMessagesStaggered(user, where, count + messagesLoaded, limit, newAfter, totalMessagesToLoad, onCompleteInterface);
        mostRecentAfter.setCurrentAfter(newAfter);
        mostRecentNumLoaded.set(count + messagesLoaded);
      } else {
        onCompleteInterface.onComplete(mostRecentAfter.currentAfter, mostRecentNumLoaded.get());
      }
    });

  }

  public void downloadAllPastMessages(RedditAccount user, String where, int count, int limit, String after, OnCompleteMessageLoad onCompleteInterface) {
    AfterMessageHolder mostRecentAfter = new AfterMessageHolder(after);
    AtomicInteger mostRecentNumLoaded = new AtomicInteger();

    downloadMessages(user, where, count, limit, after, (newAfter, messagesLoaded) -> {
      if (after != null || !after.equals("null")) {
        downloadMessages(user, where, count + messagesLoaded, limit, newAfter, onCompleteInterface);
        System.out.println("Downloading all past messages recursed. Count is " + (count + messagesLoaded) + " and newest after loaded is " + newAfter);
        mostRecentAfter.setCurrentAfter(newAfter);
        mostRecentNumLoaded.set(count + messagesLoaded);
      } else {
        onCompleteInterface.onComplete(mostRecentAfter.currentAfter, mostRecentNumLoaded.get());
      }
    });
  }

  public interface OnCompleteMessageLoad {
    void onComplete(String after, int messagesLoaded);
  }

  private static class AfterMessageHolder {
    private String currentAfter;

    public AfterMessageHolder(String currentAfter) {
      this.currentAfter = currentAfter;
      Log.v("AfterMessageHolder", "Created with " + currentAfter);
    }

    public void setCurrentAfter(String currentAfter) {
      this.currentAfter = currentAfter;
    }

    public String getCurrentAfter() {
      return currentAfter;
    }
  }

}
