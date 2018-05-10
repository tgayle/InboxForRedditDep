package app.endershrooms.inboxforreddit3.fragments;

import android.app.Fragment;
import app.endershrooms.inboxforreddit3.RxBus;

/**
 * Created by Travis on 4/26/2018.
 */

public abstract class BaseFragment extends Fragment {

  @Override
  public void onDestroy() {
    super.onDestroy();
    RxBus.unregister(this);
  }
}
