package app.endershrooms.inboxforreddit3;

import android.content.Context;
import database.DatabaseWrapper;
import okhttp3.OkHttpClient;

/**
 * Created by Travis on 1/22/2018.
 */

public class Singleton {

  private static Singleton thisSingleton;
  private DatabaseWrapper db = DatabaseWrapper.get();
  private OkHttpClient client = new OkHttpClient();


  public static Singleton getInstance() {
    if (thisSingleton == null) {
      thisSingleton = new Singleton();
    }
    return thisSingleton;
  }

  public DatabaseWrapper prepareDatabase(Context context) {
    db.prepareDatabase(context);
    return db;
  }

  public DatabaseWrapper getDb() {
    return db;
  }

  public OkHttpClient client() {
    return client;
  }


}
