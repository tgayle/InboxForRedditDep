package app.endershrooms.inboxforreddit3.viewmodels.model;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.paging.PagedList;
import app.endershrooms.inboxforreddit3.models.reddit.Message;
import app.endershrooms.inboxforreddit3.models.reddit.RedditAccount;
import app.endershrooms.inboxforreddit3.repositories.MessagesRepository;
import app.endershrooms.inboxforreddit3.repositories.UserRepository;
import java.util.List;

public class MessagesActivityDataModel {
  private UserRepository userRepo = UserRepository.get();
  private MessagesRepository messageRepo = MessagesRepository.get();

  private MutableLiveData<String> currentUserName = new MutableLiveData<>();
  private LiveData<RedditAccount> currentAccount = Transformations
      .switchMap(currentUserName, userRepo::getAccount);

  public LiveData<RedditAccount> getCurrentAccount() {
    return currentAccount;
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
  public LiveData<PagedList<Message>> getMessagesForConversationView() {
    return messageRepo.getNewestMessagesPerConversation(currentAccount.getValue());
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

  public LiveData<List<Message>> getUnreadMessagesAsList(RedditAccount account) {
    return messageRepo.getUnreadMessagesAsListForAccount(account);
  }
}
