package app.endershrooms.inboxforreddit3.adapters;

import android.arch.paging.PagedListAdapter;
import android.support.annotation.NonNull;
import android.util.Log;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Travis on 1/22/2018.
 */

public class ConversationPreviewAdapter extends
    PagedListAdapter<Message, PreviewViewHolder> implements SelectableItems<PreviewViewHolder, Message> {

  private OnMessageSelectedInterface onMessageSelectedInterface;

  private HashMap<PreviewViewHolder, Message> selectedItems = new HashMap<>();
  private AtomicInteger numItemsSelected = new AtomicInteger();
  private boolean itemSelectionModeEnabled = false;

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
    if (getCurrentList() == null) return 0;
    return getCurrentList().size();
  }

  @Override
  public int getNumberOfItemsSelected() {
    return numItemsSelected.get();
  }

  @Override
  public List<Message> getSelectedItems() {
    return new ArrayList<>(selectedItems.values());
  }

  @Override
  public void removeSelectedItemByKey(PreviewViewHolder previewViewHolder) {
    selectedItems.remove(previewViewHolder);
  }

  @Override
  public void removeSelectedItem(Message item) {

  }

  @Override
  public void clearSelectedItems() {
    for (PreviewViewHolder previewViewHolder : selectedItems.keySet()) {
      if (previewViewHolder != null) previewViewHolder.setMessageSelected(false);
    }
    selectedItems.clear();
    numItemsSelected.set(0);
  }

  public void allowItemSelection(boolean enabled) {
    itemSelectionModeEnabled = enabled;
  }

  @Override
  public void markItemSelected(PreviewViewHolder vh, Message item) {
    if (selectedItems.containsKey(vh)) {
      removeSelectedItemByKey(vh);
      Log.d("PreviewAdapter", "Num selected: " + numItemsSelected.decrementAndGet());
      vh.setMessageSelected(false);
      return;
    }
    selectedItems.put(vh, item);
    Log.d("PreviewAdapter", "Num selected: " + numItemsSelected.incrementAndGet());
    vh.setMessageSelected(true);
  }

  public class PreviewViewHolder extends MessageViewHolder {

    public PreviewViewHolder(View itemView) {
      super(itemView, 2);
      allowItemSelection = true;
    }

    @Override
    public OnClickListener onMessageClick(Message message) {
      return view -> {
        //Go to full conversation fragment.
        if (itemSelectionModeEnabled) {
          markItemSelected(this, message);
        } else {
          onMessageSelectedInterface.onMessageSelected(message);
        }
      };
    }

    @Override
    public OnLongClickListener onMessageLongClick(Message message) {
      return view -> {
        if (itemSelectionModeEnabled) markItemSelected(this, message);
        onMessageSelectedInterface.onMessageLongSelect(this, message);
        return true;
      };
    }
  }
}
