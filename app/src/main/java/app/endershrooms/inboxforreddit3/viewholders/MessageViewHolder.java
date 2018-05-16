package app.endershrooms.inboxforreddit3.viewholders;

import static app.endershrooms.inboxforreddit3.MiscFuncs.noTrailingwhiteLines;
import static app.endershrooms.inboxforreddit3.MiscFuncs.trim;

import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.util.DiffUtil;
import android.support.v7.util.DiffUtil.ItemCallback;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import app.endershrooms.inboxforreddit3.MiscFuncs;
import app.endershrooms.inboxforreddit3.R;
import app.endershrooms.inboxforreddit3.models.reddit.Message;

public abstract class MessageViewHolder extends ViewHolder {

  View parentCardView;
  TextView subjectTv;
  TextView usernameTv;
  TextView dateTv;
  ImageView sentReceivedIv;
  TextView messageTv;

  public MessageViewHolder(View itemView) {
    super(itemView);
  }

  public abstract View.OnClickListener onMessageClick(Message message);

  public void bind(Message message) {
    clear();
    String trimmedMsg = message.getMessageBody().trim();
    this.subjectTv.setText(message.getSubject());
    if (message.getMessageOwner().equals(message.getAuthor())) {
      this.usernameTv.setText(message.getDestination());
      this.sentReceivedIv.setRotation(180f); //sent top-right angle
    } else {
      this.usernameTv.setText(message.getAuthor());
      this.sentReceivedIv.setRotation(0f); //received
    }

    if (message.getNew()) {
      this.usernameTv.setTextColor(ContextCompat.getColor(this.usernameTv.getContext(), R.color.unread_message));
    } else {
      this.usernameTv.setTextColor(Color.BLACK);
    }

    this.dateTv.setText(MiscFuncs.getRelativeDateTime(message.getTimestamp()));

    this.messageTv.setText(trim(Html.fromHtml(noTrailingwhiteLines(trimmedMsg)))); //Not trimming a second time adds weird whitespace?
    this.messageTv.setMaxLines(2);
    this.parentCardView.setOnClickListener(onMessageClick(message));
  }

  public void clear() {
    parentCardView.setVisibility(View.VISIBLE);
    parentCardView.setOnClickListener(null);
    subjectTv.setText("");
    usernameTv.setText("");
    dateTv.setText("");
    sentReceivedIv.setRotation(0f);
    messageTv.setText("");

  }

  public void hide() {
    parentCardView.setVisibility(View.GONE);
  }

  public static final DiffUtil.ItemCallback<Message> MESSAGE_DIFFERENCE_CALCULATOR =
      new ItemCallback<Message>() {
        @Override
        public boolean areItemsTheSame(Message oldItem, Message newItem) {
          return oldItem.getMessageName().equals(newItem.getMessageName());
        }

        @Override
        public boolean areContentsTheSame(Message oldItem, Message newItem) {
          return oldItem.equals(newItem);
        }
      };
}
