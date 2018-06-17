package app.endershrooms.inboxforreddit3.interfaces;

import android.support.v7.widget.RecyclerView.ViewHolder;
import app.endershrooms.inboxforreddit3.models.reddit.Message;

public interface OnMessageSelectedInterface {

  void onMessageSelected(Message message);

  default void onMessageLongSelect(
      ViewHolder previewViewHolder, Message message) {};
}
