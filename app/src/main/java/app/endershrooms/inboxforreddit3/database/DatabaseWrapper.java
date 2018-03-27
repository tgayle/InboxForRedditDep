package app.endershrooms.inboxforreddit3.database;

import android.arch.persistence.room.Room;
import android.content.Context;

/**
 * Created by Travis on 3/23/2018.
 */

public class DatabaseWrapper {

  private static DatabaseWrapper singleton;

  public static DatabaseWrapper get() {
    return singleton;
  }

  AppDatabase db;

  public void prepareDatabase(Context context) {
    if (db == null) {
      db = Room.databaseBuilder(context, AppDatabase.class, "InboxForRedditDB.db").build();
    }
  }

}
