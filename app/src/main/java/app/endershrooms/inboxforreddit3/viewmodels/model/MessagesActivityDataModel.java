package app.endershrooms.inboxforreddit3.viewmodels.model;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.paging.PagedList;
import app.endershrooms.inboxforreddit3.MiscFuncs;
import app.endershrooms.inboxforreddit3.models.reddit.Message;
import app.endershrooms.inboxforreddit3.models.reddit.RedditAccount;
import app.endershrooms.inboxforreddit3.repositories.MessagesRepository;
import app.endershrooms.inboxforreddit3.repositories.UserRepository;
import java.util.List;

public class MessagesActivityDataModel {
  private UserRepository userRepo = UserRepository.get();
  private MessagesRepository messageRepo = MessagesRepository.get();

  private RedditAccount localCurrentAccount;
  private MutableLiveData<String> currentUserName = new MutableLiveData<>();
  private MediatorLiveData<RedditAccount> mediatorAccountLiveData = new MediatorLiveData<>();

  public MessagesActivityDataModel(String initialUsername) {
      currentUserName.setValue(initialUsername);

    LiveData<RedditAccount> currentAccountDbObserver = Transformations
        .switchMap(currentUserName, userRepo::getAccount);

    mediatorAccountLiveData.addSource(currentAccountDbObserver, newAccount -> {
        if (MiscFuncs.shouldCurrentAccountBeReplaced(localCurrentAccount, newAccount)) {
          if (newAccount == null) {
            mediatorAccountLiveData.setValue(MiscFuncs.ANON_ACCOUNT);
          } else {
            mediatorAccountLiveData.setValue(newAccount);
          }
          localCurrentAccount = newAccount;
        }
      });
  }

  public LiveData<RedditAccount> getCurrentAccount() {
    return mediatorAccountLiveData;
  }

  public LiveData<String> getCurrentUserName() {
    return currentUserName;
  }

  public MutableLiveData<String> getCurrentUserNameAsMutable() {
    return currentUserName;
  }

  public LiveData<List<RedditAccount>> getAccounts() {
    return userRepo.getAccounts();
  }

  public LiveData<RedditAccount> getAccount(String username) {
    return userRepo.getAccount(username);
  }

  public LiveData<PagedList<RedditAccount>> getAccountsAsPagedList() {
    return userRepo.getAccountsAsPagedList();
  }

  //Returns a list of conversations with the oldest messages at top.
  public LiveData<PagedList<Message>> getMessagesInInboxForConversationView() {
    return Transformations.switchMap(mediatorAccountLiveData, messageRepo::getNewestMessagesPerConversationInInbox);
  }

  public LiveData<PagedList<Message>> getAllConversationMessagesPaged(RedditAccount user, String parentName) {
    return messageRepo.getMessagesForConversationPaged(user, parentName);
  }

  public UserRepository getUserRepo() {
    return userRepo;
  }

  public MessagesRepository getMessageRepo() {
    return messageRepo;
  }

  public LiveData<List<Message>> getUnreadMessagesAsList() {
    return Transformations.switchMap(mediatorAccountLiveData, messageRepo::getUnreadMessagesAsListForAccount);
  }

  public LiveData<Boolean> markAllUnreadMessagesAsRead() {
    if (mediatorAccountLiveData.getValue() == null) {
      return new MutableLiveData<>();
    }
    return messageRepo.markAllMessagesAsRead(mediatorAccountLiveData.getValue());
  }

  public LiveData<Integer> restoreAllDeletedMessagesForAccount() {
    return messageRepo.restoreAllDeletedMessagesForAccount(localCurrentAccount);
  }
}
