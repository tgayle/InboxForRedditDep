package app.endershrooms.inboxforreddit3.viewmodels;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;
import app.endershrooms.inboxforreddit3.Constants;
import app.endershrooms.inboxforreddit3.Singleton;
import app.endershrooms.inboxforreddit3.repositories.UserRepository;


/**
 *
 * Created by Travis on 5/8/2018.
 */

public class EntryLoginActivityViewModel extends BaseLoginViewModel {
  private UserRepository userRepo = UserRepository.get();

  public EntryLoginActivityViewModel(@NonNull Application application) {
    super(application);
    Singleton.get().prepareDatabase(getApplication().getApplicationContext());
    sharedPreferences = getApplication().getSharedPreferences(Constants.SHARED_PREFERENCES_MAIN,
        Context.MODE_PRIVATE);

    String currentUser = sharedPreferences.getString(Constants.SHARED_PREFS_CURRENT_ACC, null);
    if (currentUser != null) {
      setAddedAccount(userRepo.getAccount(currentUser).getValue());
    }
  }
}
