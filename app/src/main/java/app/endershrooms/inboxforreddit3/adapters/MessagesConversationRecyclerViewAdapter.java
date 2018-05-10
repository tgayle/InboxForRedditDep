package app.endershrooms.inboxforreddit3.adapters;

import static app.endershrooms.inboxforreddit3.MiscFuncs.noTrailingwhiteLines;
import static app.endershrooms.inboxforreddit3.MiscFuncs.trim;

import android.arch.paging.PagedListAdapter;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.util.DiffUtil;
import android.support.v7.util.DiffUtil.ItemCallback;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import app.endershrooms.inboxforreddit3.MiscFuncs;
import app.endershrooms.inboxforreddit3.R;
import app.endershrooms.inboxforreddit3.models.reddit.Message;

/**
 * Created by Travis on 1/22/2018.
 */

public class MessagesConversationRecyclerViewAdapter extends
    PagedListAdapter<Message, Viewholder> {

  public MessagesConversationRecyclerViewAdapter() {
    super(CONVERSATION_DIFF_CALCULATOR);
  }

  @Override
  public Viewholder onCreateViewHolder(ViewGroup parent, int viewType) {
    return new Viewholder(LayoutInflater.from(parent.getContext())
        .inflate(R.layout.card_message_rv_linear, parent, false));
  }

  @Override
  public void onBindViewHolder(@NonNull Viewholder vh, int position) {
    Message message = getItem(position); //Last Message
    if (message != null) {
      vh.bind(message);
    } else {
      vh.hide();
    }
  }

  @Override
  public int getItemCount() {
    if (getCurrentList()== null) return 0;
    return getCurrentList().size();
  }

  private static final DiffUtil.ItemCallback<Message> CONVERSATION_DIFF_CALCULATOR =
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

  class Viewholder extends ViewHolder {

    View parentCardView;
    TextView subjectTv;
    TextView usernameTv;
    TextView dateTv;
    ImageView sentReceivedIv;
    TextView messageTv;


    public Viewholder(View itemView) {
      super(itemView);

      parentCardView = itemView.findViewById(R.id.conversation_layout_cardview);
      subjectTv = (TextView) itemView.findViewById(R.id.conversation_layout_subject);
      usernameTv = (TextView) itemView.findViewById(R.id.conversation_layout_username);
      dateTv = (TextView) itemView.findViewById(R.id.conversation_layout_postdate);
      sentReceivedIv = (ImageView) itemView.findViewById(R.id.conversation_layout_sentreceived);
      messageTv = (TextView) itemView.findViewById(R.id.conversation_layout_content);
    }

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
      this.parentCardView.setOnClickListener(
          v -> {
            Toast.makeText(v.getContext(), "Conversation Clicked " + this.getAdapterPosition(), Toast.LENGTH_SHORT).show();
          });
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
  }
