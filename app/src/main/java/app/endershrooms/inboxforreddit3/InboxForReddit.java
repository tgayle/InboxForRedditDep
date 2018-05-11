package app.endershrooms.inboxforreddit3;

import android.app.Application;

public class InboxForReddit extends Application {

  @Override
  public void onCreate() {
    super.onCreate();
    Singleton.get().prepareDatabase(getApplicationContext());
  }
}
