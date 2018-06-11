package app.endershrooms.inboxforreddit3;

import android.os.Bundle;

public class BundleCreator {

  private Bundle bundle;
  public BundleCreator(Bundle holder) {
    this.bundle = holder;
  }

  public BundleCreator putString(String key, String value) {
    bundle.putString(key, value);
    return this;
  }

  public BundleCreator putInt(String key, int value) {
    bundle.putInt(key, value);
    return this;
  }

  public Bundle build() {
    return bundle;
  }
}
