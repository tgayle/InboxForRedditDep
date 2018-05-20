package app.endershrooms.inboxforreddit3.fragments;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import app.endershrooms.inboxforreddit3.R;
import app.endershrooms.inboxforreddit3.adapters.ConversationFullAdapter;
import app.endershrooms.inboxforreddit3.models.reddit.RedditAccount;
import app.endershrooms.inboxforreddit3.viewmodels.MessagesActivityViewModel;
import app.endershrooms.inboxforreddit3.views.CustomLinearLayoutManager;
import java.util.concurrent.atomic.AtomicBoolean;

public class ConversationViewFragment extends Fragment {

  public ConversationViewFragment() {
    // Required empty public constructor
  }

  public static ConversationViewFragment newInstance() {
    return new ConversationViewFragment();
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    MessagesActivityViewModel model = ViewModelProviders.of(getActivity()).get(MessagesActivityViewModel.class);
    RedditAccount currentAccount = model.getCurrentAccount().getValue();
    Log.d("ConversationViewFrag", "Current account is " + currentAccount);

    RecyclerView messages = getView().findViewById(R.id.messages_list);
    messages.setLayoutManager(new CustomLinearLayoutManager(getContext()));
    ConversationFullAdapter adapter = new ConversationFullAdapter();
    messages.setAdapter(adapter);

    AtomicBoolean didFirstScrollToBottom = new AtomicBoolean(false);
    model.getAllConversationMessagesPaged(currentAccount, model.getCurrentConversationName().getValue())
    .observe(this, list -> {
      adapter.submitList(list);
      if (!didFirstScrollToBottom.get()) {
        messages.scrollToPosition((adapter.getItemCount() == 0) ? 0 : adapter.getItemCount() - 1);
        didFirstScrollToBottom.set(true);
      }
    });

  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    return inflater.inflate(R.layout.fragment_conversation_view, container, false);
  }


}
