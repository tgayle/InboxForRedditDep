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
  String SELECT_ALL_FROM_ALL_ACCOUNTS = "SELECT * FROM messages";
  String SELECT_NEWEST_MESSAGE_PER_CONVERSATION_FOR_ACCOUNT = "SELECT t1.* FROM messages AS t1"
      + " JOIN (SELECT parentMessageName, MAX(timestamp) timestamp FROM messages GROUP BY parentMessageName) AS t2"
      + " ON t1.parentMessageName = t2.parentMessageName AND t1.timestamp = t2.timestamp AND t1.messageOwner LIKE :account"
      + " ORDER BY timestamp";
  String SELECT_ALL_UNREAD_MESSAGES_FOR_ACCOUNT = "SELECT *"
      + " FROM messages"
      + " WHERE isNew = 1"
      + " AND messageOwner LIKE :account";

  String SELECT_NAMES_OF_ALL_UNREAD_MESSAGES_FOR_ACCOUNT = "SELECT messageName FROM messages WHERE messageOwner LIKE :account AND isNew = 1";

  String SELECT_NEWEST_MESSAGE_PER_CONVERSATIONS_ONLY_IN_INBOX_FOR_ACCOUNT = "SELECT t1.* FROM messages AS t1 "
      + "JOIN (SELECT parentMessageName, MAX(timestamp) timestamp FROM messages GROUP BY parentMessageName) AS t2 "
      + "ON t1.parentMessageName = t2.parentMessageName AND t1.timestamp = t2.timestamp AND t1.messageOwner LIKE :account "
      + "WHERE inInbox = 1 AND inDeleted = 0 AND isHidden = 0 "
      + "ORDER BY timestamp";

  String SELECT_FIRST_MESSAGE_FOR_ACCOUNT = "SELECT * FROM messages WHERE messageOwner LIKE :account LIMIT 1";
  String SELECT_NEWEST_MESSAGE_FOR_ACCOUNT = "SELECT * FROM messages WHERE messageOwner LIKE :account ORDER BY messageName DESC LIMIT 1";
  String SELECT_ALL_MESSAGES_FOR_CONVERSATION_FOR_ACCOUNT = "SELECT * FROM messages WHERE messageOwner LIKE :account AND parentMessageName LIKE :parentname ORDER BY timestamp ASC";
  String SELECT_ALL_PARENT_NAMES_FOR_ACCOUNT = "SELECT DISTINCT parentMessageName FROM messages WHERE messageOwner LIKE :account ORDER BY timestamp ASC";

  String DELETE_ALL_MESSAGES_FOR_ACCOUNT = "DELETE FROM messages WHERE messageOwner LIKE :account";

  String UPDATE_MARK_ALL_UNREAD_MESSAGES_AS_READ_FOR_ACCOUNT = "UPDATE messages SET isNew = 0 WHERE messageOwner LIKE :account AND isNew = 1";

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  Long insertMessage(Message message);

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  List<Long> insertMessages(List<Message> messages);

  @Update
  void updateMessage(Message message);

  @Update
  int updateMessages(List<Message> messages);

  @Delete
  int deleteMessage(Message message);

  @Delete
  int deleteMessages(List<Message> messages);

  @Query("SELECT * FROM messages WHERE messageOwner LIKE :account ORDER BY timestamp ASC")
  LiveData<List<Message>> getAllUserMessagesAsc(String account); //Oldest First

  @Query("SELECT * FROM messages WHERE messageOwner LIKE :account ORDER BY timestamp DESC")
  LiveData<List<Message>> getAllUserMessagesDesc(String account); //Newest First

  @Query(SELECT_ALL_FROM_ALL_ACCOUNTS)
  LiveData<List<Message>> getAllMessagesFromAllAccounts();

  @Query(SELECT_ALL_PARENT_NAMES_FOR_ACCOUNT)
  LiveData<List<String>> getAllParentConversationNamesForAccount(String account);

  @Query(SELECT_ALL_MESSAGES_FOR_CONVERSATION_FOR_ACCOUNT)
  LiveData<List<Message>> getAllMessagesFromConversation(String account, String parentname);

  @Query(SELECT_ALL_MESSAGES_FOR_CONVERSATION_FOR_ACCOUNT)
  DataSource.Factory<Integer, Message> getAllMessagesFromConversationAsPaged(String account,
      String parentname);

  @Query(SELECT_NEWEST_MESSAGE_FOR_ACCOUNT)
  LiveData<Message> getNewestMessageInDatabase(String account);

  @Query(SELECT_NEWEST_MESSAGE_FOR_ACCOUNT)
  Single<Message> getNewestMessageInDatabaseAsSingle(String account);

  @Query(SELECT_FIRST_MESSAGE_FOR_ACCOUNT)
  Message getFirstMessage(String account);

  //https://stackoverflow.com/questions/10999522/how-to-get-the-latest-record-in-each-group-using-group-by
  @Query(SELECT_NEWEST_MESSAGE_PER_CONVERSATION_FOR_ACCOUNT)
  DataSource.Factory<Integer, Message> getNewestMessageForAllConversationsForUserPagable(
      String account);

  @Query(SELECT_ALL_UNREAD_MESSAGES_FOR_ACCOUNT)
  LiveData<Message> getUnreadMessagesForAccount(String account);

  @Query(SELECT_ALL_UNREAD_MESSAGES_FOR_ACCOUNT)
  LiveData<List<Message>> getUnreadMessagesAsListForAccount(String account);

  @Query(SELECT_NAMES_OF_ALL_UNREAD_MESSAGES_FOR_ACCOUNT)
  Single<List<String>> getNamesOfAllUnreadMessagesForAccount(String account);

  @Query(SELECT_ALL_UNREAD_MESSAGES_FOR_ACCOUNT)
  DataSource.Factory<Integer, Message> getUnreadMessagesForAccountPagable(String account);

  @Query(DELETE_ALL_MESSAGES_FOR_ACCOUNT)
  int deleteAllMessagesForAccount(String account);

  @Query(UPDATE_MARK_ALL_UNREAD_MESSAGES_AS_READ_FOR_ACCOUNT)
  int markAllUnreadMessagesAsReadForAccount(String account);

  @Query(SELECT_NEWEST_MESSAGE_PER_CONVERSATIONS_ONLY_IN_INBOX_FOR_ACCOUNT)
  LiveData<List<Message>> selectNewestMessageForAllConversationsInInboxForAccount(String account);

  @Query(SELECT_NEWEST_MESSAGE_PER_CONVERSATIONS_ONLY_IN_INBOX_FOR_ACCOUNT)
  DataSource.Factory<Integer, Message> selectNewestMessageForAllConversationsInInboxForAccountPagable(String account);

}
