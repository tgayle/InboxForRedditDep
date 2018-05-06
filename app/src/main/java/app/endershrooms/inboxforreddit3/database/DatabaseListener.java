package app.endershrooms.inboxforreddit3.database;

import app.endershrooms.inboxforreddit3.RxBus;
import app.endershrooms.inboxforreddit3.RxBus.Subjects;
import app.endershrooms.inboxforreddit3.Singleton;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Travis on 4/26/2018.
 */

public class DatabaseListener {

  private static final DatabaseListener ourInstance = new DatabaseListener();

  public static DatabaseListener getInstance() {
    return ourInstance;
  }

  private DatabaseListener() {

  }

  public void prepareListeners() {
    Singleton.get().getDb().accounts().getAccountsFlowable()
        .subscribeOn(Schedulers.io())
        .subscribe(account -> {
          RxBus.publish(Subjects.ON_ACCOUNT_UPDATE, account);
        });
    //FIXME:
  }


}
