package app.endershrooms.inboxforreddit3.database.dao;

import static app.endershrooms.inboxforreddit3.database.dao.MessageDao.SELECT_ALL_MESSAGES_FOR_CONVERSATION_FOR_ACCOUNT;
import static app.endershrooms.inboxforreddit3.database.dao.MessageDao.SELECT_ALL_UNREAD_MESSAGES_FOR_ACCOUNT;
import static app.endershrooms.inboxforreddit3.database.dao.MessageDao.SELECT_NEWEST_MESSAGE_PER_CONVERSATIONS_ONLY_IN_INBOX_FOR_ACCOUNT;
import static app.endershrooms.inboxforreddit3.database.dao.MessageDao.SELECT_NEWEST_MESSAGE_PER_CONVERSATION_FOR_ACCOUNT;

import android.arch.paging.DataSource;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;
import app.endershrooms.inboxforreddit3.models.reddit.Message;

@Dao
public interface PagedMessageDao {

  @Query(SELECT_NEWEST_MESSAGE_PER_CONVERSATIONS_ONLY_IN_INBOX_FOR_ACCOUNT)
  abstract DataSource.Factory<Integer, Message> selectNewestMessageForAllConversationsInInboxForAccountPagable(String account);

  @Query(SELECT_ALL_UNREAD_MESSAGES_FOR_ACCOUNT)
  abstract DataSource.Factory<Integer, Message> getUnreadMessagesForAccountPagable(String account);

  //https://stackoverflow.com/questions/10999522/how-to-get-the-latest-record-in-each-group-using-group-by
  @Query(SELECT_NEWEST_MESSAGE_PER_CONVERSATION_FOR_ACCOUNT)
  abstract DataSource.Factory<Integer, Message> getNewestMessageForAllConversationsForUserPagable(
      String account);

  @Query(SELECT_ALL_MESSAGES_FOR_CONVERSATION_FOR_ACCOUNT)
  abstract DataSource.Factory<Integer, Message> getAllMessagesFromConversationAsPaged(String account,
      String parentname);

}
