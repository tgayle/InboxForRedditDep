package app.endershrooms.inboxforreddit3.adapters;

import static app.endershrooms.inboxforreddit3.MiscFuncs.noTrailingwhiteLines;
import static app.endershrooms.inboxforreddit3.MiscFuncs.trim;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import app.endershrooms.inboxforreddit3.MiscFuncs;
import app.endershrooms.inboxforreddit3.R;
import app.endershrooms.inboxforreddit3.models.Message;
import java.util.List;

/**
 * Created by Travis on 1/22/2018.
 */

public class MessagesRecyclerViewAdapter extends
    RecyclerView.Adapter<MessagesRecyclerViewAdapter.Viewholder> {

  List<Message> messages;

  public MessagesRecyclerViewAdapter(List<Message> messages) {
    this.messages = messages;
    Log.v("Adapter", "created with size " + messages.size());
  }

  @Override
  public Viewholder onCreateViewHolder(ViewGroup parent, int viewType) {
    return new Viewholder(LayoutInflater.from(parent.getContext())
        .inflate(R.layout.card_message_rv_linear, parent, false));
  }

  @Override
  public void onBindViewHolder(Viewholder vh, int position) {
    final Message message = messages.get(vh.getAdapterPosition());
    final String trimmedMsg = message.getMessageBody().trim();

    vh.subjectTv.setText(message.getSubject());
    vh.usernameTv.setText(message.getAuthor());
    vh.dateTv.setText(MiscFuncs.getRelativeDateTime(message.getTimestamp()));

    vh.messageTv.setText(trim(Html.fromHtml(noTrailingwhiteLines(trimmedMsg)))); //Not trimming a second time adds weird whitespace?
    vh.parentCardView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        debugLog(message.getMessageName(), noTrailingwhiteLines(message.getMessageBody()));
      }
    });

  }

  public void addMessage(Message message) {
    this.messages.add(message);
    notifyItemInserted(messages.size());
  }

  @Override
  public int getItemCount() {
    return messages.size();
  }

  public void addMessages(List<Message> messages) {
    int initSize = this.messages.size();
    Log.v("Adapter", "pre "+ this.messages.size());
    this.messages.addAll(messages);
    Log.v("Adapter", "post "+ this.messages.size());

    notifyDataSetChanged();
//    notifyItemRangeInserted(initSize, messages.size());
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
