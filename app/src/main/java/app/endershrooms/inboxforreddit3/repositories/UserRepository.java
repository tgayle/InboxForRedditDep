package app.endershrooms.inboxforreddit3.repositories;

import android.arch.lifecycle.LiveData;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;
import app.endershrooms.inboxforreddit3.Singleton;
import app.endershrooms.inboxforreddit3.account.Authentication;
import app.endershrooms.inboxforreddit3.database.dao.AccountDao;
import app.endershrooms.inboxforreddit3.models.reddit.RedditAccount;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import java.util.List;

/**
 * Created by Travis on 5/7/2018.
 */

public class UserRepository {

  private static final UserRepository ourInstance = new UserRepository();
  public static UserRepository get() {
    return ourInstance;
  }
  private final AccountDao accountDao = Singleton.get().getDb().accounts();
  private UserRepository() {
  }

  public LiveData<RedditAccount> getAccount(String account) {
    return accountDao.getAccountFromName(account);
  }

  public LiveData<List<RedditAccount>> getAccounts() {
    return accountDao.getAccountsAsLiveData();
  }

  public LiveData<PagedList<RedditAccount>> getAccountsAsPagedList() {
    return new LivePagedListBuilder<>(accountDao.getAccountsAsPagedList(), 10).build();
  }

  public void removeAccount(RedditAccount account) {
    Single.fromCallable(() -> accountDao.removeAccount(account))
        .subscribeOn(Schedulers.io())
        .subscribe();

    Singleton.get().getRedditApi().revokeUserToken(Authentication.basicAuthorizationHeader, account.getRefreshToken().toString(), "refresh_token")
        .observeOn(Schedulers.io())
        .subscribe();
  }

  public void updateAccount(RedditAccount acc) {
    Single.fromCallable(() -> accountDao.updateAccount(acc))
        .subscribeOn(Schedulers.io())
        .subscribe();
  }
}
