package database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import app.endershrooms.inboxforreddit3.models.Message;
import app.endershrooms.inboxforreddit3.models.RedditAccount;
import database.dao.AccountDao;
import database.dao.MessageDao;

/**
 * Created by Travis on 3/23/2018.
 */
@Database(entities = {RedditAccount.class, Message.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase{
  public abstract AccountDao accounts();
  public abstract MessageDao messages();
}
