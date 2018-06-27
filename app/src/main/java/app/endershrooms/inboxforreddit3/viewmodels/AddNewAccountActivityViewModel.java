package app.endershrooms.inboxforreddit3.viewmodels;

import android.app.Application;
import android.support.annotation.NonNull;
import app.endershrooms.inboxforreddit3.viewmodels.login.BaseLoginViewModel;

/**
 * ViewModel for adding a isNew account when we already have accounts. No custom implementation here
 *  but this is left in case of the future.
 * Created by Travis on 5/8/2018.
 */

public class AddNewAccountActivityViewModel extends BaseLoginViewModel {

  public AddNewAccountActivityViewModel(
      @NonNull Application application) {
    super(application);
  }
}
