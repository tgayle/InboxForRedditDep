package app.endershrooms.inboxforreddit3.viewmodels;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import app.endershrooms.inboxforreddit3.Constants;
import app.endershrooms.inboxforreddit3.Singleton;
import app.endershrooms.inboxforreddit3.models.reddit.RedditAccount;
import app.endershrooms.inboxforreddit3.repositories.UserRepository;
import java.util.List;


/**
 *
 * Created by Travis on 5/8/2018.
 */

public class EntryLoginActivityViewModel extends BaseLoginViewModel {

  static final String TAG = "EntryLoginViewModel";
  private UserRepository userRepo;
  private String sharedPrefUser;

  public EntryLoginActivityViewModel(@NonNull Application application) {
    super(application);
    Singleton.get().prepareDatabase(getApplication().getApplicationContext());
    sharedPreferences = getApplication().getSharedPreferences(Constants.SHARED_PREFERENCES_MAIN,
        Context.MODE_PRIVATE);

    userRepo = UserRepository.get();
    sharedPrefUser = sharedPreferences.getString(Constants.SHARED_PREFS_CURRENT_ACC, null);
    Log.d(TAG, "Shared Pref user is currently " + sharedPrefUser);
  }

  public LiveData<List<RedditAccount>> getAllAccounts() {
    return userRepo.getAccounts();
  }

  public String getSharedPrefUser() {
    return sharedPrefUser;
  }

  public void onUserDetected(RedditAccount user) {
    Log.d(TAG, "User detect called with " + user.getUsername());
    if (user.getUsername().equals(sharedPrefUser)) {
      setAddedAccount(user);
    }
  }
}
