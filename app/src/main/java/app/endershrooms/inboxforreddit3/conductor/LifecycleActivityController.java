package app.endershrooms.inboxforreddit3.conductor;

import android.support.v4.app.FragmentActivity;
import com.bluelinelabs.conductor.archlifecycle.LifecycleController;

public abstract class LifecycleActivityController extends LifecycleController {

  public FragmentActivity getLifecycleActivity() {
    return getRouter() != null ? (FragmentActivity) getRouter().getActivity() : null;
  }
}
