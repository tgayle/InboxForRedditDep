package app.endershrooms.inboxforreddit3.adapters;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import app.endershrooms.inboxforreddit3.MiscFuncs;
import app.endershrooms.inboxforreddit3.R;
import app.endershrooms.inboxforreddit3.models.Message;

import static app.endershrooms.inboxforreddit3.MiscFuncs.noTrailingwhiteLines;

/**
 * Created by Travis on 1/22/2018.
 */

public class MessagesRecyclerViewAdapter extends RecyclerView.Adapter<MessagesRecyclerViewAdapter.Viewholder> {

    List<Message> messages;

    public MessagesRecyclerViewAdapter(List<Message> messages) {
        this.messages = messages;
    }

    @Override
    public Viewholder onCreateViewHolder(ViewGroup parent, int viewType) {
      return new Viewholder(LayoutInflater.from(parent.getContext()).inflate(R.layout.card_message_rv_linear, parent, false));
//        return new Viewholder(LayoutInflater.from(parent.getContext()).inflate(R.layout.card_message_rv, parent, false));
    }

    @Override
    public void onBindViewHolder(Viewholder vh, int position) {
      final Message message = messages.get(vh.getAdapterPosition());

      vh.subjectTv.setText(message.getSubject());
      vh.usernameTv.setText(message.getAuthor());
      vh.dateTv.setText(MiscFuncs.getRelativeDateTime(message.getTimestamp()));

//      CharSequence sequence = //trim(Html.fromHtml(noTrailingwhiteLines(message.getMessageBody())));
//      vh.messageTv.setText(sequence);
//      vh.messageTv.setText(Html.fromHtml(noTrailingwhiteLines(message.getMessageBody())));
      vh.messageTv.setText(message.getMessageBody());
      vh.parentCardView.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          debugLog(message.getMessageName(), noTrailingwhiteLines(message.getMessageBody()));
        }
      });

    }

    @Override
    public int getItemCount() {
        return messages.size();
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
