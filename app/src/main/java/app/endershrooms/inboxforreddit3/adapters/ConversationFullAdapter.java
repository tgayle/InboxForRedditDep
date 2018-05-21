package app.endershrooms.inboxforreddit3.adapters;

import android.arch.paging.PagedListAdapter;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import app.endershrooms.inboxforreddit3.R;
import app.endershrooms.inboxforreddit3.adapters.ConversationFullAdapter.ConversationMessageViewHolder;
import app.endershrooms.inboxforreddit3.models.reddit.Message;
import app.endershrooms.inboxforreddit3.viewholders.MessageViewHolder;

public class ConversationFullAdapter extends PagedListAdapter<Message, ConversationMessageViewHolder> {

  public ConversationFullAdapter() {
    super(ConversationMessageViewHolder.MESSAGE_DIFFERENCE_CALCULATOR);
  }

  @NonNull
  @Override
  public ConversationMessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    return new ConversationMessageViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.card_message_rv_linear, parent, false));
  }

  @Override
  public void onBindViewHolder(@NonNull ConversationMessageViewHolder holder, int position) {
    Message thisMsg = getItem(position);
    holder.bind(thisMsg);
  }

  @Override
  public int getItemCount() {
    if (getCurrentList() == null) return 0;
    return getCurrentList().size();
  }

  class ConversationMessageViewHolder extends MessageViewHolder {

    public ConversationMessageViewHolder(View itemView) {
      super(itemView);
      messageTv.setEllipsize(null);
    }

    @Override
    public String getUsernameTVText(Message message) {
      return message.getAuthor();
    }

    @Override
    public OnClickListener onMessageClick(Message message) {
      return null;
    }
  }
}
