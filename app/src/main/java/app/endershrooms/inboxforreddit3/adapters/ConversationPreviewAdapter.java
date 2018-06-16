package app.endershrooms.inboxforreddit3.adapters;

import android.arch.paging.PagedListAdapter;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import app.endershrooms.inboxforreddit3.R;
import app.endershrooms.inboxforreddit3.adapters.ConversationPreviewAdapter.PreviewViewHolder;
import app.endershrooms.inboxforreddit3.interfaces.OnMessageSelectedInterface;
import app.endershrooms.inboxforreddit3.models.reddit.Message;
import app.endershrooms.inboxforreddit3.viewholders.MessageViewHolder;

/**
 * Created by Travis on 1/22/2018.
 */

public class ConversationPreviewAdapter extends
    PagedListAdapter<Message, PreviewViewHolder> {

  OnMessageSelectedInterface onMessageSelectedInterface;

  public ConversationPreviewAdapter(OnMessageSelectedInterface onMessageSelectedInterface) {
    super(PreviewViewHolder.MESSAGE_DIFFERENCE_CALCULATOR);
    this.onMessageSelectedInterface = onMessageSelectedInterface;
  }

  @Override
  public PreviewViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    return new PreviewViewHolder(LayoutInflater.from(parent.getContext())
        .inflate(R.layout.card_message_rv_linear, parent, false));
  }

  @Override
  public void onBindViewHolder(@NonNull PreviewViewHolder vh, int position) {
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

  public class PreviewViewHolder extends MessageViewHolder {

    public PreviewViewHolder(View itemView) {
      super(itemView, 2);
    }

    @Override
    public OnClickListener onMessageClick(Message message) {
      return view -> {
        //Go to full conversation fragment.
        onMessageSelectedInterface.onMessageSelected(message);
      };
    }

    @Override
    public OnLongClickListener onMessageLongClick(Message message) {
      return view -> {
        onMessageSelectedInterface.onMessageLongSelect(message);
        return true;
      };
    }
  }
}
