package database.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;
import app.endershrooms.inboxforreddit3.models.Message;
import java.util.List;

/**
 * Created by Travis on 3/23/2018.
 */

@Dao
public interface MessageDao {

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  public void insertMessage(Message message);

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  public void insertMessages(List<Message> messages);

  @Update
  public void updateMessage(Message message);

  @Update
  public void updateMessages(List<Message> messages);

  @Delete
  public int deleteMessage(Message message);

  @Delete
  public int deleteMessages(List<Message> messages);

  @Query("SELECT * FROM messages WHERE messageOwner LIKE :account ORDER BY timestamp ASC")
  public List<Message> getAllUserMessages(String account);

  @Query("SELECT * FROM messages")
  public List<Message> getAllMessagesFromAllAccounts();

  @Query("SELECT * FROM messages WHERE messageOwner LIKE :account AND parentMessageName LIKE :parentname ORDER BY timestamp ASC")
  public List<Message> getAllMessagesFromConversation(String account, String parentname);
}
