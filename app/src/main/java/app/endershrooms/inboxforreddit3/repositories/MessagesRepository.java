package app.endershrooms.inboxforreddit3.repositories;

/**
 * Created by Travis on 5/7/2018.
 */

public class MessagesRepository {

  private static final MessagesRepository ourInstance = new MessagesRepository();
  public static MessagesRepository get() {
    return ourInstance;
  }
  private MessagesRepository() {
  }
}
