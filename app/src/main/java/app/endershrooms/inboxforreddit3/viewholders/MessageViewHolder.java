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

  public View parentCardView;
  public TextView subjectTv;
  public TextView usernameTv;
  public TextView dateTv;
  public ImageView sentReceivedIv;
  public TextView messageTv;

  public MessageViewHolder(View itemView) {
    super(itemView);
    parentCardView = itemView.findViewById(R.id.conversation_layout_cardview);
    subjectTv = (TextView) itemView.findViewById(R.id.conversation_layout_subject);
    usernameTv = (TextView) itemView.findViewById(R.id.conversation_layout_username);
    dateTv = (TextView) itemView.findViewById(R.id.conversation_layout_postdate);
    sentReceivedIv = (ImageView) itemView.findViewById(R.id.conversation_layout_sentreceived);
    messageTv = (TextView) itemView.findViewById(R.id.conversation_layout_content);
  }

  public MessageViewHolder(View itemView, int messageMaxLines) {
    this(itemView);
    messageTv.setMaxLines(messageMaxLines);
  }

  public abstract View.OnClickListener onMessageClick(Message message);

  public String getUsernameTVText(Message message) {
    if (message.didCurrentUserSendMessage()) {
      return message.getDestination();
    } else {
      return message.getAuthor();
    }
  }

  public void bind(Message message) {
    if (message == null) {
      hide();
      return;
    }
    clear();
    String trimmedMsg = message.getMessageBody().trim();
    this.subjectTv.setText(message.getSubject());

    if (message.didCurrentUserSendMessage()) {
      this.sentReceivedIv.setRotation(180f); //sent top-right angle
    } else {
      this.sentReceivedIv.setRotation(0f); //received
    }
    this.usernameTv.setText(getUsernameTVText(message));

    if (message.getNew()) {
      this.usernameTv.setTextColor(ContextCompat.getColor(this.usernameTv.getContext(), R.color.unread_message));
    } else {
      this.usernameTv.setTextColor(Color.BLACK);
    }

    this.dateTv.setText(MiscFuncs.getRelativeDateTime(message.getTimestamp()));

    this.messageTv.setText(trim(Html.fromHtml(noTrailingwhiteLines(trimmedMsg)))); //Not trimming a second time adds weird whitespace?
    this.parentCardView.setOnClickListener(onMessageClick(message));
    this.parentCardView.setOnLongClickListener(onMessageLongClick(message));
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

  public View.OnLongClickListener onMessageLongClick(Message message) {
    return null;
  }
}
