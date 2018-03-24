package app.endershrooms.inboxforreddit3;

import android.arch.persistence.room.Room;
import android.content.Context;
import database.AppDatabase;
import okhttp3.OkHttpClient;

/**
 * Created by Travis on 1/22/2018.
 */

public class Singleton {

  private static Singleton thisSingleton;
  private AppDatabase db;
  private OkHttpClient client = new OkHttpClient();


  public static Singleton getInstance() {
    if (thisSingleton == null) {
      thisSingleton = new Singleton();
    }
    return thisSingleton;
  }

  public AppDatabase prepareDatabase(Context context) {
    if (db == null) {
      db = Room.databaseBuilder(context, AppDatabase.class, "InboxForRedditDB").build();
    }
    return db;
  }

  public AppDatabase getDb() {
    return db;
  }

  public OkHttpClient client() {
    return client;
  }


}
