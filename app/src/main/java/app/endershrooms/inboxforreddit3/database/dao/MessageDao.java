package app.endershrooms.inboxforreddit3.database.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;
import app.endershrooms.inboxforreddit3.models.Message;
import io.reactivex.Flowable;
import io.reactivex.Single;
import java.util.List;

/**
 * Created by Travis on 3/23/2018.
 */

@Dao
public interface MessageDao {

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  public Long insertMessage(Message message);

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  public List<Long> insertMessages(List<Message> messages);

  @Update
  public void updateMessage(Message message);

  @Update
  public void updateMessages(List<Message> messages);

  @Delete
  public int deleteMessage(Message message);

  @Delete
  public int deleteMessages(List<Message> messages);

  @Query("SELECT * FROM messages WHERE messageOwner LIKE :account ORDER BY timestamp ASC")
  public Single<List<Message>> getAllUserMessagesAsc(String account); //Oldest First

  @Query("SELECT * FROM messages WHERE messageOwner LIKE :account ORDER BY timestamp DESC")
  public Single<List<Message>> getAllUserMessagesDesc(String account); //Newest First

  @Query("SELECT * FROM messages")
  public Flowable<List<Message>> getAllMessagesFromAllAccounts();

  @Query("SELECT DISTINCT parentMessageName FROM messages WHERE messageOwner LIKE :account ORDER BY timestamp ASC")
  public Single<List<String>> getAllParentConversationNamesForAccount(String account);

  @Query("SELECT * FROM messages WHERE messageOwner LIKE :account AND parentMessageName LIKE :parentname ORDER BY timestamp ASC")
  public Flowable<List<Message>> getAllMessagesFromConversation(String account, String parentname);

  @Query("SELECT * FROM messages WHERE messageOwner LIKE :account ORDER BY messageName DESC LIMIT 1")
  public Single<Message> getNewestMessageInDatabase(String account);

  @Query("SELECT * FROM messages WHERE messageOwner LIKE :account LIMIT 1")
  public Single<List<Message>> getFirstMessage(String account);

  //https://stackoverflow.com/questions/10999522/how-to-get-the-latest-record-in-each-group-using-group-by
  @Query("SELECT t1.* FROM messages AS t1"
      + " JOIN (SELECT parentMessageName, MAX(timestamp) timestamp FROM messages GROUP BY parentMessageName) AS t2"
      + " ON t1.parentMessageName = t2.parentMessageName AND t1.timestamp = t2.timestamp AND t1.messageOwner LIKE :account"
      + " ORDER BY timestamp")
  public Single<List<Message>> getNewestMessageForAllConversationsForUser(String account);
}
