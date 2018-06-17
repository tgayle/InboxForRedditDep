package app.endershrooms.inboxforreddit3.repositories;

import android.annotation.SuppressLint;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;
import android.util.Log;
import app.endershrooms.inboxforreddit3.Singleton;
import app.endershrooms.inboxforreddit3.database.LocalMessageStates;
import app.endershrooms.inboxforreddit3.database.dao.MessageDao;
import app.endershrooms.inboxforreddit3.models.reddit.Message;
import app.endershrooms.inboxforreddit3.models.reddit.RedditAccount;
import app.endershrooms.inboxforreddit3.models.reddit.ResponseWithError;
import app.endershrooms.inboxforreddit3.net.APIManager;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import java.util.List;

/**
 * Created by Travis on 5/7/2018.
 */

public class MessagesRepository {
  private final MessageDao messageDao = Singleton.get().getDb().messages();

  private static final MessagesRepository ourInstance = new MessagesRepository();
  public static MessagesRepository get() {
    return ourInstance;
  }
  private MessagesRepository() {

  }

  public LiveData<PagedList<Message>> getNewestMessagesPerConversation(RedditAccount user) {
    return new LivePagedListBuilder<>(messageDao.getNewestMessageForAllConversationsForUserPagable(user.getUsername()), 10).build();
  }

  public LiveData<PagedList<Message>> getNewestMessagesPerConversationInInbox(RedditAccount user) {
    return new LivePagedListBuilder<>(messageDao.selectNewestMessageForAllConversationsInInboxForAccountPagable(user.getUsername()), 10).build();
  }

  @SuppressLint("CheckResult")
  public LiveData<ResponseWithError<String, Throwable>> loadNewestMessages(RedditAccount user) {
    MutableLiveData<ResponseWithError<String, Throwable>> result = new MutableLiveData<>();
    ResponseWithError<String, Throwable> response = new ResponseWithError<>(null, null);

    messageDao.getNewestMessageInDatabaseAsSingle(user.getUsername())
        .subscribeOn(Schedulers.io())
        .subscribe(newestMsg -> {
          Log.d("MessageRepo", "Init newest message is " + (newestMsg == null ? newestMsg : newestMsg.getMessageName()));
          String nameToStartFrom = newestMsg == null ? "" : newestMsg.getMessageName();
          APIManager.get().downloadAllFutureMessagesAllLocations(user, 20, nameToStartFrom,
              (beforeOrAfter, after, messagesLoaded) -> {
                response.setData(after);
                result.postValue(response);
//                Log.d("MessageRepo", "Load newest message " + beforeOrAfter +" is " + after);
              }, throwable -> {
                response.setError(throwable);
                result.postValue(response);
              });
        }, err -> {
          response.setError(err);
          result.postValue(response);
        });
    //Get newest message

    return result;
  }

  public LiveData<ResponseWithError<String, Throwable>> loadAllMessages(RedditAccount user) {
    MutableLiveData<ResponseWithError<String, Throwable>> result = new MutableLiveData<>();
    //String is the after value of a reddit response
    ResponseWithError<String, Throwable> response = new ResponseWithError<>(null, null);

    APIManager.get().downloadAllPastMessagesAllLocations(user, 20,
        (beforeOrAfter, after, messagesLoaded) -> {
          response.setData(after);
          result.postValue(response);
        }, throwable -> {
          response.setError(throwable);
          result.postValue(response);
        });
    return result;
  }

  public LiveData<PagedList<Message>> getUnreadMessagesPagedForAccount(RedditAccount account) {
    return new LivePagedListBuilder<>(messageDao.getUnreadMessagesForAccountPagable(account.getUsername()), 20).build();
  }

  public LiveData<PagedList<Message>> getMessagesForConversationPaged(RedditAccount user, String parentName) {
    return new LivePagedListBuilder<>(messageDao.getAllMessagesFromConversationAsPaged(user.getUsername(), parentName), 20).build();
  }

  public LiveData<Message> getUnreadMessagesForAccount(RedditAccount acc) {
    return messageDao.getUnreadMessagesForAccount(acc.getUsername());
  }

  public LiveData<List<Message>> getUnreadMessagesAsListForAccount(RedditAccount acc) {
    return messageDao.getUnreadMessagesAsListForAccount(acc.getUsername());
  }

  public LiveData<List<Message>> getAllMessagesForAccount(RedditAccount user) {
   return messageDao.getAllUserMessagesAsc(user.getUsername());
  }

  public void removeAccountMessages(RedditAccount removedAccount) {
    Single.fromCallable(() -> messageDao.deleteAllMessagesForAccount(removedAccount.getUsername()))
        .subscribeOn(Schedulers.io())
        .subscribe(removed -> {
          Log.d("RemoveMessages", removedAccount.getUsername() + " had " + removed + " messages removed");
        });
  }

  public LiveData<Boolean> markAllMessagesAsRead(RedditAccount account) {
    MediatorLiveData<Boolean> onMessagesFinishedMarkRead = new MediatorLiveData<>();
    messageDao.getNamesOfAllUnreadMessagesForAccount(account.getUsername())
        .observeOn(Schedulers.io())
        .subscribeOn(Schedulers.io())
        .subscribe(names -> {
          onMessagesFinishedMarkRead.addSource(
              APIManager.get().markAllUnreadMessagesAsRead(account, names),
              result -> onMessagesFinishedMarkRead.setValue(result));
          Single.fromCallable(() -> messageDao.markAllUnreadMessagesAsReadForAccount(account.getUsername()))
              .observeOn(Schedulers.io())
              .subscribe();
        });
    return onMessagesFinishedMarkRead;
  }

  public void hideMessages(List<Message> selectedItems) {
    for (Message selectedItem : selectedItems) {
      selectedItem.getMessageState().removeAllStatesExceptOne(LocalMessageStates.DELETED);
    }
    Single.fromCallable(() -> messageDao.updateMessages(selectedItems))
        .observeOn(Schedulers.io())
        .subscribeOn(Schedulers.io())
        .subscribe(numUpdated -> {
          Log.d("MessagesRepo", "Messages moved to deleted " + numUpdated);
        });
  }
}
