package app.endershrooms.inboxforreddit3.database.dao;

import android.arch.lifecycle.LiveData;
import android.arch.paging.DataSource;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;
import app.endershrooms.inboxforreddit3.models.reddit.Message;
import io.reactivex.Single;
import java.util.List;

/**
 * Created by Travis on 3/23/2018.
 */

@Dao
public interface MessageDao {
  static final String SELECT_ALL_FOR_ACCOUNT = "SELECT * FROM messages";
  static final String SELECT_NEWEST_MESSAGE_PER_CONVERSATION_FOR_ACCOUNT = "SELECT t1.* FROM messages AS t1"
      + " JOIN (SELECT parentMessageName, MAX(timestamp) timestamp FROM messages GROUP BY parentMessageName) AS t2"
      + " ON t1.parentMessageName = t2.parentMessageName AND t1.timestamp = t2.timestamp AND t1.messageOwner LIKE :account"
      + " ORDER BY timestamp";
  static final String SELECT_ALL_UNREAD_MESSAGES_FOR_ACCOUNT = "SELECT *"
      + " FROM messages"
      + " WHERE isNew = 1"
      + " AND messageOwner LIKE :account";
  static final String SELECT_FIRST_MESSAGE_FOR_ACCOUNT = "SELECT * FROM messages WHERE messageOwner LIKE :account LIMIT 1";
  static final String SELECT_NEWEST_MESSAGE_FOR_ACCOUNT = "SELECT * FROM messages WHERE messageOwner LIKE :account ORDER BY messageName DESC LIMIT 1";
  static final String SELECT_ALL_MESSAGES_FOR_CONVERSATION_FOR_ACCOUNT = "SELECT * FROM messages WHERE messageOwner LIKE :account AND parentMessageName LIKE :parentname ORDER BY timestamp ASC";
  static final String SELECT_ALL_PARENT_NAMES_FOR_ACCOUNT = "SELECT DISTINCT parentMessageName FROM messages WHERE messageOwner LIKE :account ORDER BY timestamp ASC";

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
  public LiveData<List<Message>> getAllUserMessagesAsc(String account); //Oldest First

  @Query("SELECT * FROM messages WHERE messageOwner LIKE :account ORDER BY timestamp DESC")
  public LiveData<List<Message>> getAllUserMessagesDesc(String account); //Newest First

  @Query(SELECT_ALL_FOR_ACCOUNT)
  public LiveData<List<Message>> getAllMessagesFromAllAccounts();

  @Query(SELECT_ALL_PARENT_NAMES_FOR_ACCOUNT)
  public LiveData<List<String>> getAllParentConversationNamesForAccount(String account);

  @Query(SELECT_ALL_MESSAGES_FOR_CONVERSATION_FOR_ACCOUNT)
  public LiveData<List<Message>> getAllMessagesFromConversation(String account, String parentname);

  @Query(SELECT_NEWEST_MESSAGE_FOR_ACCOUNT)
  public LiveData<Message> getNewestMessageInDatabase(String account);

  @Query(SELECT_NEWEST_MESSAGE_FOR_ACCOUNT)
  public Single<Message> getNewestMessageInDatabaseAsSingle(String account);

  @Query(SELECT_FIRST_MESSAGE_FOR_ACCOUNT)
  public Message getFirstMessage(String account);

  //https://stackoverflow.com/questions/10999522/how-to-get-the-latest-record-in-each-group-using-group-by
  @Query(SELECT_NEWEST_MESSAGE_PER_CONVERSATION_FOR_ACCOUNT)
  public DataSource.Factory<Integer, Message> getNewestMessageForAllConversationsForUserPagable(String account);

  @Query(SELECT_ALL_UNREAD_MESSAGES_FOR_ACCOUNT)
  public LiveData<Message> getUnreadMessagesForAccount(String account);

  @Query(SELECT_ALL_UNREAD_MESSAGES_FOR_ACCOUNT)
  public DataSource.Factory<Integer, Message> getUnreadMessagesForAccountPagable(String account);
}
