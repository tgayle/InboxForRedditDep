package app.endershrooms.inboxforreddit3.interfaces;

import app.endershrooms.inboxforreddit3.models.reddit.Message;

public interface OnMessageSelectedInterface {

  void onMessageSelected(Message message);

  default void onMessageLongSelect(Message message) {};
}
