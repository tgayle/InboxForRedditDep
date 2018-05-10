package app.endershrooms.inboxforreddit3.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import app.endershrooms.inboxforreddit3.database.dao.AccountDao;
import app.endershrooms.inboxforreddit3.database.dao.MessageDao;
import app.endershrooms.inboxforreddit3.models.reddit.Message;
import app.endershrooms.inboxforreddit3.models.reddit.RedditAccount;

/**
 * Created by Travis on 3/23/2018.
 */
@Database(entities = {RedditAccount.class, Message.class}, version = 3)
public abstract class AppDatabase extends RoomDatabase{
  public abstract AccountDao accounts();
  public abstract MessageDao messages();
}
