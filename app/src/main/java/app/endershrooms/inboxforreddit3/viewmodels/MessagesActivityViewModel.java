package app.endershrooms.inboxforreddit3.viewmodels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.paging.PagedList;
import app.endershrooms.inboxforreddit3.models.reddit.Conversation;
import app.endershrooms.inboxforreddit3.models.reddit.Message;
import app.endershrooms.inboxforreddit3.models.reddit.RedditAccount;
import app.endershrooms.inboxforreddit3.models.reddit.ResponseWithError;
import app.endershrooms.inboxforreddit3.repositories.MessagesRepository;
import app.endershrooms.inboxforreddit3.repositories.UserRepository;
import java.util.List;

/**
 * Created by Travis on 5/7/2018.
 */

public class MessagesActivityViewModel extends ViewModel {
  private UserRepository userRepo = UserRepository.get();
  private MessagesRepository messageRepo = MessagesRepository.get();
  private MutableLiveData<RedditAccount> currentAccount = new MutableLiveData<>();
  private MutableLiveData<ResponseWithError<LoadingStatusEnum, String>> loadingStatus = new MutableLiveData<>();
  //TODO: Use shared preferences to pass current user along.

  public void setCurrentAccount(String userName) {
    currentAccount.postValue(userRepo.getAccount(userName).getValue());
  }

  public LiveData<RedditAccount> getCurrentAccount() {
    return currentAccount;
  }

  public LiveData<List<RedditAccount>> getAccounts() {
    return userRepo.getAccounts();
  }

  public LiveData<PagedList<RedditAccount>> getAccountsAsPagedList() {
    return userRepo.getAccountsAsPagedList();
  }

  //Returns a list of conversations with the oldest messages at top.
  public LiveData<PagedList<Message>> getMessagesForConversationView() {
    return messageRepo.getNewestMessagesPerConversation(currentAccount.getValue());
  }

  public void loadNewestMessages() {
    messageRepo.loadNewestMessages(currentAccount.getValue());
  }

  public void loadAllMessages() {

  }

  public LiveData<PagedList<Conversation>> getConversations() {
    messageRepo.
  }

  public LiveData<ResponseWithError<LoadingStatusEnum, String>> getLoadingStatus() {
    return loadingStatus;
  }

  public void setLoadingStatus(LoadingStatusEnum status) {
    loadingStatus.postValue(new ResponseWithError<>(status, null));
  }

  public enum LoadingStatusEnum {
    LOADING,
    ERROR
  }
}
