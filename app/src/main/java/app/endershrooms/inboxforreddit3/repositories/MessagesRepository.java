package app.endershrooms.inboxforreddit3.repositories;

import android.arch.lifecycle.LiveData;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;
import app.endershrooms.inboxforreddit3.Singleton;
import app.endershrooms.inboxforreddit3.database.dao.MessageDao;
import app.endershrooms.inboxforreddit3.models.reddit.Message;
import app.endershrooms.inboxforreddit3.models.reddit.RedditAccount;
import app.endershrooms.inboxforreddit3.models.reddit.ResponseWithError;
import app.endershrooms.inboxforreddit3.net.APIManager;

/**
 * Created by Travis on 5/7/2018.
 */

public class MessagesRepository {
  private final MessageDao messageDao = Singleton.get().getDb().messages();

  private static final MessagesRepository ourInstance = new MessagesRepository();
  public static MessagesRepository get() {
    return ourInstance;
  }
  private MessagesRepository() {

  }

  public LiveData<PagedList<Message>> getNewestMessagesPerConversation(RedditAccount user) {
    return new LivePagedListBuilder<>(messageDao.getNewestMessageForAllConversationsForUser(user.getUsername()), 10).build();
  }

  public ResponseWithError<Void, Throwable> loadNewestMessages(RedditAccount user) {
    //TODO: finish loading newest messages and make sure to update changes to old messages by checking currently unread messages
    APIManager.get().downloadAllFutureMessagesAllLocations(user, 20, "", );
  }


}
