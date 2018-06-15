package app.endershrooms.inboxforreddit3.database;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.migration.Migration;
import android.support.annotation.NonNull;
import app.endershrooms.inboxforreddit3.database.dao.AccountDao;
import app.endershrooms.inboxforreddit3.database.dao.MessageDao;
import app.endershrooms.inboxforreddit3.models.reddit.Message;
import app.endershrooms.inboxforreddit3.models.reddit.RedditAccount;

/**
 * Created by Travis on 3/23/2018.
 */
@Database(entities = {RedditAccount.class, Message.class}, version = 4)
public abstract class AppDatabase extends RoomDatabase {
  public abstract AccountDao accounts();
  public abstract MessageDao messages();

  public static Migration MIGRATION_3_4 = new Migration(3, 4) {
    @Override
    public void migrate(@NonNull SupportSQLiteDatabase database) {

    }
  };
}
