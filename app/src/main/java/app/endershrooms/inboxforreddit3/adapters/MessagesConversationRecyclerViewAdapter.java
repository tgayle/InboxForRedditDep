package app.endershrooms.inboxforreddit3.adapters;

import static app.endershrooms.inboxforreddit3.MiscFuncs.noTrailingwhiteLines;
import static app.endershrooms.inboxforreddit3.MiscFuncs.trim;

import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import app.endershrooms.inboxforreddit3.MiscFuncs;
import app.endershrooms.inboxforreddit3.R;
import app.endershrooms.inboxforreddit3.models.Conversation;
import app.endershrooms.inboxforreddit3.models.Message;
import java.util.List;

/**
 * Created by Travis on 1/22/2018.
 */

public class MessagesConversationRecyclerViewAdapter extends
    RecyclerView.Adapter<MessagesConversationRecyclerViewAdapter.Viewholder> {

  private List<Conversation> conversations;

  public MessagesConversationRecyclerViewAdapter(List<Conversation> convos) {
    this.conversations = convos;
    Log.v("ConversationAdapter", "created with size " + conversations.size());
  }

  @Override
  public Viewholder onCreateViewHolder(ViewGroup parent, int viewType) {
    return new Viewholder(LayoutInflater.from(parent.getContext())
        .inflate(R.layout.card_message_rv_linear, parent, false));
  }

  @Override
  public void onBindViewHolder(Viewholder vh, int position) {
    Conversation thisConvo = conversations.get(vh.getAdapterPosition());
    Message message = thisConvo.getMessages().get(thisConvo.getMessages().size() - 1); //Last Message
    String trimmedMsg = message.getMessageBody().trim();
    vh.subjectTv.setText(message.getSubject());
    if (message.getMessageOwner().equals(message.getAuthor())) {
      vh.usernameTv.setText(message.getDestination());
      vh.sentReceivedIv.setRotation(180f);
    } else {
      vh.usernameTv.setText(message.getAuthor());
      vh.sentReceivedIv.setRotation(0f);
    }

    if (message.getNew()) {
      vh.usernameTv.setTextColor(ContextCompat.getColor(vh.usernameTv.getContext(), R.color.unread_message));
    } else {
      vh.usernameTv.setTextColor(Color.BLACK);
    }

    vh.dateTv.setText(MiscFuncs.getRelativeDateTime(message.getTimestamp()));

    vh.messageTv.setText(trim(Html.fromHtml(noTrailingwhiteLines(trimmedMsg)))); //Not trimming a second time adds weird whitespace?
    vh.messageTv.setMaxLines(2);
    vh.parentCardView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Toast.makeText(v.getContext(), "Conversation Clicked " + vh.getAdapterPosition(), Toast.LENGTH_SHORT).show();
      }
    });

  }

  public void addConversation(Conversation convo) {
    this.conversations.add(convo);
    notifyItemInserted(conversations.size());
  }

  @Override
  public int getItemCount() {
    return conversations.size();
  }

  public void addConversations(List<Conversation> convos) {
    int initSize = this.conversations.size();
    Log.v("ConvoAdapter", "pre "+ this.conversations.size());
    this.conversations.addAll(convos);
    Log.v("ConvoAdapter", "post "+ this.conversations.size());
//    notifyDataSetChanged();
    notifyItemRangeInserted(initSize, conversations.size());
  }

  public void animateRemoval() {
//    TODO: Proper animation.
    conversations.clear();
    notifyDataSetChanged();
  }

  public void clearAndReplaceConversations(List<Conversation> conversations) {
//    animateRemoval();
////    conversations.clear();
    this.conversations.clear();
    this.conversations.addAll(conversations);
    notifyDataSetChanged();
  }

  public void updateConversations(List<Conversation> newConversations) {

    for (int i = 0; i < conversations.size(); i++) {
      for (int x = 0; x < newConversations.size(); x++) {
         Conversation currentConvo = conversations.get(i);
         Conversation newConvo = newConversations.get(x);
        if (currentConvo.getParentName().equals(newConvo.getParentName())
            && currentConvo.getMessages().size() < newConvo.getMessages().size()) {
            conversations.set(i, newConvo);
            notifyItemChanged(i);
        }
      }
    }
    notifyDataSetChanged();
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
  }

  public static void debugLog(String tag, String log) {
    if (log.length() > 4000) {
      Log.d(tag, log.substring(0, 4000));
      debugLog(tag, log.substring(4000));
    } else {
      Log.d(tag, log);
    }
  }
}
