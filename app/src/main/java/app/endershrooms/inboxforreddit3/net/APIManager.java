package app.endershrooms.inboxforreddit3.net;

import static java.lang.annotation.RetentionPolicy.SOURCE;

import android.annotation.SuppressLint;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.StringDef;
import android.util.Log;
import app.endershrooms.inboxforreddit3.Singleton;
import app.endershrooms.inboxforreddit3.account.Authentication;
import app.endershrooms.inboxforreddit3.account.Token.AccessToken;
import app.endershrooms.inboxforreddit3.interfaces.OnCompleteInterface;
import app.endershrooms.inboxforreddit3.models.reddit.RedditAccount;
import app.endershrooms.inboxforreddit3.net.model.MessagesJSONResponse;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import java.lang.annotation.Retention;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Travis on 3/27/2018.
 */

public class APIManager {

  private static APIManager apiManager;
  private RedditEndpoint redditService = Singleton.get().getRedditApi();

  public static synchronized APIManager get() {
    if (apiManager == null) {
      apiManager = new APIManager();
    }
    return apiManager;
  }

  @SuppressLint("CheckResult")
  private void updateUserToken(RedditAccount user, OnCompleteInterface listener, OnRedditApiError errListener) {
    if (user.getAccessToken() != null && !user.getAccessToken().isTokenExpired()) {
      listener.onComplete();
      return;
    }

    Singleton
        .get().getRedditApi().getAccessTokenFromCode(Authentication.basicAuthorizationHeader, new Authentication.Params.RefreshParams(user.getRefreshToken()))
        .observeOn(Schedulers.io())
        .subscribe(jsonLoginResponse -> {
          user.setAccessToken(new AccessToken(jsonLoginResponse.access_token, jsonLoginResponse.expires_in));
          Singleton.get().getDb().accounts().updateAccount(user);
          listener.onComplete();
        }, err -> {
          errListener.onError(err);
        });
  }

  @Retention(SOURCE)
  @StringDef({"before", "after"})
  @interface Subject {}
  private void downloadMessages(RedditAccount user, String where, int limit, @Subject String beforeOrAfter,
      String beforeAfter, OnCompleteMessageLoad onCompleteInterface, OnRedditApiError errorListener) {

    updateUserToken(user, () -> {
      Observable<MessagesJSONResponse> messageLoader = beforeOrAfter.equals("before") ?
          redditService.getMessagesWithBefore(user.getAuthentication(), where, limit, beforeAfter) :
          redditService.getMessagesWithAfter(user.getAuthentication(), where, limit, beforeAfter);

      messageLoader
          .observeOn(Schedulers.io())
          .subscribe(messagesJSONResponse -> {
            Singleton.get().getDb().messages().insertMessages(messagesJSONResponse.convertJsonToMessages(user));
            onCompleteInterface.onComplete(beforeOrAfter, messagesJSONResponse.data.after, messagesJSONResponse.data.children.size());
          }, errorListener::onError);
    }, errorListener::onError);

  }

  private void downloadAllPastMessages(RedditAccount user, String where, int limit, String after, OnCompleteMessageLoad onCompleteInterface, OnRedditApiError errorListener) {
    BeforeAfterMessageHolder mostRecentAfter = new BeforeAfterMessageHolder(after);
    AtomicInteger mostRecentNumLoaded = new AtomicInteger();

    downloadMessages(user, where, limit, "after", after, (beforeOrAfter, newAfter, messagesLoaded) -> {
      if (newAfter != null && !newAfter.equals("null")) {
        downloadAllPastMessages(user, where, limit, newAfter, onCompleteInterface, errorListener);
        mostRecentAfter.setCurrent(newAfter);
        mostRecentNumLoaded.set(messagesLoaded);
      } else {
        Log.d("DownloadMessages", "Finished load? " + newAfter);
        onCompleteInterface.onComplete(beforeOrAfter, newAfter, mostRecentNumLoaded.get());
      }
    }, errorListener);
  }

  private void downloadUnreadMessages(RedditAccount user, int limit, String after, OnCompleteMessageLoad onCompleteMessageLoad, OnRedditApiError errorListener) {
    BeforeAfterMessageHolder mostRecentAfter = new BeforeAfterMessageHolder(after);
    AtomicInteger mostRecentNumLoaded = new AtomicInteger();
    downloadMessages(user, "unread", limit, "after", after, (beforeOrAfter, newAfter, messagesLoaded) -> {
        if (newAfter != null) {
          downloadUnreadMessages(user, limit, newAfter, onCompleteMessageLoad, errorListener);
          mostRecentAfter.setCurrent(newAfter);
          mostRecentNumLoaded.set(messagesLoaded);
        } else {
          onCompleteMessageLoad.onComplete(beforeOrAfter, mostRecentAfter.current, mostRecentNumLoaded.get());
        }
      }, errorListener);
  }

  private OnCompleteMessageLoad completeLoadCollector(int numExpectedToFinish, OnCompleteMessageLoad finalCaller) {
    AtomicInteger numFinished = new AtomicInteger(0);
    return new OnCompleteMessageLoad() {
      @Override
      public void onComplete(String beforeOrAfter, String after, int messagesLoaded) {
        Log.d("Complete Loader", "After called as " + after);
        if (after == null || after.equals("null")) {
          if (numFinished.incrementAndGet() == numExpectedToFinish) {
            finalCaller.onComplete(beforeOrAfter, after, messagesLoaded);
          }
        }
      }
    };
  }

  private void downloadAllFutureMessages(RedditAccount user, String where, int limit, String before, OnCompleteMessageLoad onCompleteMessageLoad, OnRedditApiError errorListener) {
    BeforeAfterMessageHolder mostRecentBefore = new BeforeAfterMessageHolder(before);
    AtomicInteger mostRecentNumLoaded = new AtomicInteger();

    downloadUnreadMessages(user, limit, "", onCompleteMessageLoad, errorListener);

    downloadMessages(user, where, limit, "before", before, (beforeOrAfter, newBefore, messagesLoaded) -> {
      if (before != null) {
        downloadAllFutureMessages(user, where, limit, newBefore, onCompleteMessageLoad, errorListener);
        //System.out.println("Downloading all past messages recursed. Count is " + (messagesLoaded) + " and newest " + beforeOrAfter + " loaded is " + newBefore);
        mostRecentBefore.setCurrent(newBefore);
        mostRecentNumLoaded.set(messagesLoaded);
      } else {
        onCompleteMessageLoad.onComplete("before", mostRecentBefore.current, mostRecentNumLoaded.get());
      }
    }, errorListener);
  }

  //After must be an empty string instead of null here to make sure progress is properly sent to the rest of the app.
  public void downloadAllPastMessagesAllLocations(RedditAccount user, int limit, OnCompleteMessageLoad onCompleteMessageLoad, OnRedditApiError errorListener) {
    OnCompleteMessageLoad collector = completeLoadCollector(2, onCompleteMessageLoad);
    downloadAllPastMessages(user, "inbox", limit, "", collector, errorListener);
    downloadAllPastMessages(user, "sent", limit, "", collector, errorListener);
  }

  public void downloadAllFutureMessagesAllLocations(RedditAccount user, int limit, String before, OnCompleteMessageLoad onCompleteMessageLoad, OnRedditApiError errorListener) {
    downloadAllFutureMessages(user, "inbox", limit, before, onCompleteMessageLoad, errorListener);
    downloadAllFutureMessages(user, "sent", limit, before, onCompleteMessageLoad, errorListener);
    downloadUnreadMessages(user, limit, null, onCompleteMessageLoad, errorListener);
  }

  public LiveData<Boolean> markAllUnreadMessagesAsRead(RedditAccount user, List<String> ids) {
    MutableLiveData<Boolean> onCompletedResult = new MutableLiveData<>();
    updateUserToken(user, () -> {
      StringBuilder allIdsAsOneString = new StringBuilder();
      for (String id : ids) {
        allIdsAsOneString.append(id).append(",");
      }
      redditService.markMessageAsRead(user.getAuthentication(), allIdsAsOneString.toString())
          .observeOn(Schedulers.io())
          .subscribe(onCompleteResponse -> {
            onCompletedResult.postValue(true);
          });
    }, throwable -> {

    });
    return onCompletedResult;
  }

  public interface OnCompleteMessageLoad {
    void onComplete(String beforeOrAfter, String after, int messagesLoaded);
  }

  private static class BeforeAfterMessageHolder {
    private String current;

    BeforeAfterMessageHolder(String currentAfter) {
      this.current = currentAfter;
    }

    void setCurrent(String currentAfter) {
      this.current = currentAfter;
    }

    public String getCurrent() {
      return current;
    }
  }

}
