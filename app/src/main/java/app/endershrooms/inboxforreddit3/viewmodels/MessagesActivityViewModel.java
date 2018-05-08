package app.endershrooms.inboxforreddit3.viewmodels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.paging.PagedList;
import app.endershrooms.inboxforreddit3.models.RedditAccount;
import app.endershrooms.inboxforreddit3.repositories.MessagesRepository;
import app.endershrooms.inboxforreddit3.repositories.UserRepository;
import java.util.List;

/**
 * Created by Travis on 5/7/2018.
 */

public class MessagesActivityViewModel extends ViewModel {
  private UserRepository userRepo = UserRepository.get();
  private MessagesRepository messageRepo = MessagesRepository.get();
  private LiveData<RedditAccount> currentAccount;
  //TODO: Use shared preferences to pass current user along.

  public void setCurrentAccount(String userName) {
    this.currentAccount = userRepo.getAccount(userName);
    //TODO: LiveData transform/map to make sure views know when account changes?
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


}
