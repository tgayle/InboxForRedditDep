package app.endershrooms.inboxforreddit3.activities;

import android.support.v7.app.AppCompatActivity;
import app.endershrooms.inboxforreddit3.RxBus;

/**
 * Created by Travis on 4/26/2018.
 */

public abstract class BaseActivity extends AppCompatActivity {

  @Override
  protected void onDestroy() {
    super.onDestroy();
    RxBus.unregister(this);
  }
}
