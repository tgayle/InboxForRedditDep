package app.endershrooms.inboxforreddit3.database.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import app.endershrooms.inboxforreddit3.models.RedditAccount;
import io.reactivex.Flowable;
import io.reactivex.Single;

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
  public Flowable<RedditAccount> getAccountsObservable();

  @Query("SELECT * FROM accounts WHERE username LIKE :name")
  public Single<RedditAccount> getAccountFromName(String name);
}
