package app.endershrooms.inboxforreddit3;

/**
 * Created by Travis on 3/23/2018.
 */

class RxBus {

  private static final RxBus ourInstance = new RxBus();

  static RxBus get() {
    return ourInstance;
  }


  private RxBus() {

  }
}
