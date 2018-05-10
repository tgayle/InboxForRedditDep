package app.endershrooms.inboxforreddit3.database.dao;

import android.arch.lifecycle.LiveData;
import android.arch.paging.DataSource;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;
import app.endershrooms.inboxforreddit3.models.reddit.RedditAccount;
import io.reactivex.Flowable;
import java.util.List;

/**
 * Created by Travis on 3/23/2018.
 */

@Dao
public interface AccountDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  public Long addAccount(RedditAccount account);

  @Update
  public int updateAccount(RedditAccount account);

  @Delete
  public int removeAccount(RedditAccount account);

  @Query("SELECT * FROM accounts")
  public Flowable<List<RedditAccount>> getAllAccounts();

  @Query("SELECT * FROM accounts")
  public LiveData<List<RedditAccount>> getAccountsAsLiveData();

  @Query("SELECT * FROM accounts")
  public DataSource.Factory<Integer, RedditAccount> getAccountsAsPagedList();

  @Query("SELECT * FROM accounts WHERE username LIKE :name")
  public LiveData<RedditAccount> getAccountFromName(String name);
}
