package app.endershrooms.inboxforreddit3.database.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;
import android.database.sqlite.SQLiteConstraintException;
import app.endershrooms.inboxforreddit3.database.AppDatabase;
import app.endershrooms.inboxforreddit3.models.reddit.Message;
import io.reactivex.Single;
import java.util.List;

/**
 * Created by Travis on 3/23/2018.
 */

@Dao
public abstract class MessageDao {
  private AppDatabase db;
  public MessageDao(AppDatabase appDatabase) {
    db = appDatabase;
  }

  static final String SELECT_ALL_FROM_ALL_ACCOUNTS = "SELECT * FROM messages";
  static final String SELECT_NEWEST_MESSAGE_PER_CONVERSATION_FOR_ACCOUNT = "SELECT t1.* FROM messages AS t1"
      + " JOIN (SELECT parentMessageName, MAX(timestamp) timestamp FROM messages GROUP BY parentMessageName) AS t2"
      + " ON t1.parentMessageName = t2.parentMessageName AND t1.timestamp = t2.timestamp AND t1.messageOwner LIKE :account"
      + " ORDER BY timestamp";
  static final String SELECT_ALL_UNREAD_MESSAGES_FOR_ACCOUNT = "SELECT *"
      + " FROM messages"
      + " WHERE isNew = 1"
      + " AND messageOwner LIKE :account";

  static final String SELECT_NAMES_OF_ALL_UNREAD_MESSAGES_FOR_ACCOUNT = "SELECT messageName FROM messages WHERE messageOwner LIKE :account AND isNew = 1";

  static final String SELECT_NEWEST_MESSAGE_PER_CONVERSATIONS_ONLY_IN_INBOX_FOR_ACCOUNT = "SELECT t1.* FROM messages AS t1 "
      + "JOIN (SELECT parentMessageName, MAX(timestamp) timestamp FROM messages GROUP BY parentMessageName) AS t2 "
      + "ON t1.parentMessageName = t2.parentMessageName AND t1.timestamp = t2.timestamp AND t1.messageOwner LIKE :account "
      + "WHERE inInbox = 1 AND inDeleted = 0 AND isHidden = 0 "
      + "ORDER BY timestamp";

  static final String SELECT_FIRST_MESSAGE_FOR_ACCOUNT = "SELECT * FROM messages WHERE messageOwner LIKE :account LIMIT 1";
  static final String SELECT_NEWEST_MESSAGE_FOR_ACCOUNT = "SELECT * FROM messages WHERE messageOwner LIKE :account ORDER BY messageName DESC LIMIT 1";
  static final String SELECT_ALL_MESSAGES_FOR_CONVERSATION_FOR_ACCOUNT = "SELECT * FROM messages WHERE messageOwner LIKE :account AND parentMessageName LIKE :parentname ORDER BY timestamp ASC";
  static final String SELECT_ALL_PARENT_NAMES_FOR_ACCOUNT = "SELECT DISTINCT parentMessageName FROM messages WHERE messageOwner LIKE :account ORDER BY timestamp ASC";

  static final String DELETE_ALL_MESSAGES_FOR_ACCOUNT = "DELETE FROM messages WHERE messageOwner LIKE :account";

  static final String UPDATE_MARK_ALL_UNREAD_MESSAGES_AS_READ_FOR_ACCOUNT = "UPDATE messages SET isNew = 0 WHERE messageOwner LIKE :account AND isNew = 1";

  @Insert(onConflict = OnConflictStrategy.FAIL)
  public abstract Long insertMessageQuery(Message message);

  @Insert(onConflict = OnConflictStrategy.FAIL)
  public abstract List<Long> insertMessagesQuery(List<Message> messages);

  @Update
  public abstract void updateMessage(Message message);

  @Update
  public abstract int updateMessages(List<Message> messages);

  @Delete
  public abstract int deleteMessage(Message message);

  @Delete
  public abstract int deleteMessages(List<Message> messages);

  @Query("SELECT * FROM messages WHERE messageOwner LIKE :account ORDER BY timestamp ASC")
  public abstract LiveData<List<Message>> getAllUserMessagesAsc(String account); //Oldest First

  @Query("SELECT * FROM messages WHERE messageOwner LIKE :account ORDER BY timestamp DESC")
  public abstract LiveData<List<Message>> getAllUserMessagesDesc(String account); //Newest First

  @Query(SELECT_ALL_FROM_ALL_ACCOUNTS)
  public abstract LiveData<List<Message>> getAllMessagesFromAllAccounts();

  @Query(SELECT_ALL_PARENT_NAMES_FOR_ACCOUNT)
  public abstract LiveData<List<String>> getAllParentConversationNamesForAccount(String account);

  @Query(SELECT_ALL_MESSAGES_FOR_CONVERSATION_FOR_ACCOUNT)
  public abstract LiveData<List<Message>> getAllMessagesFromConversation(String account, String parentname);

  @Query(SELECT_NEWEST_MESSAGE_FOR_ACCOUNT)
  public abstract LiveData<Message> getNewestMessageInDatabase(String account);

  @Query(SELECT_NEWEST_MESSAGE_FOR_ACCOUNT)
  public abstract Single<Message> getNewestMessageInDatabaseAsSingle(String account);

  @Query(SELECT_FIRST_MESSAGE_FOR_ACCOUNT)
  public abstract Message getFirstMessage(String account);

  @Query(SELECT_ALL_UNREAD_MESSAGES_FOR_ACCOUNT)
  public abstract LiveData<Message> getUnreadMessagesForAccount(String account);

  @Query(SELECT_ALL_UNREAD_MESSAGES_FOR_ACCOUNT)
  public abstract LiveData<List<Message>> getUnreadMessagesAsListForAccount(String account);

  @Query(SELECT_NAMES_OF_ALL_UNREAD_MESSAGES_FOR_ACCOUNT)
  public abstract Single<List<String>> getNamesOfAllUnreadMessagesForAccount(String account);

  @Query(DELETE_ALL_MESSAGES_FOR_ACCOUNT)
  public abstract int deleteAllMessagesForAccount(String account);

  @Query(UPDATE_MARK_ALL_UNREAD_MESSAGES_AS_READ_FOR_ACCOUNT)
  public abstract int markAllUnreadMessagesAsReadForAccount(String account);

  @Query(SELECT_NEWEST_MESSAGE_PER_CONVERSATIONS_ONLY_IN_INBOX_FOR_ACCOUNT)
  public abstract LiveData<List<Message>> selectNewestMessageForAllConversationsInInboxForAccount(String account);

  @Query("SELECT * FROM messages WHERE messageOwner LIKE :messageOwner AND messageName LIKE :messageName LIMIT 1")
  public abstract Message getIndividualMessageFromName(String messageOwner, String messageName);

  @Query("SELECT * FROM messages WHERE messageOwner LIKE :messageOwner AND messageName IN (:names)")
  public abstract List<Message> getMessagesMatchingName(String messageOwner, String[] names);

  @Query("UPDATE messages SET isNew = :newIsNew WHERE messageName LIKE :messageName")
  public abstract void insertMessageOnlyUpdatingIsNew(String messageName, boolean newIsNew);

  public void insertMessages(List<Message> messages) {
    for (Message message : messages) {
      try {
        insertMessageQuery(message);
      } catch (SQLiteConstraintException e) {
        insertMessageOnlyUpdatingIsNew(message.getMessageName(), message.getNew());
      }
    }

  }

}
