package app.endershrooms.inboxforreddit3.fragments;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import app.endershrooms.inboxforreddit3.MiscFuncs;
import app.endershrooms.inboxforreddit3.R;
import app.endershrooms.inboxforreddit3.adapters.ConversationFullAdapter;
import app.endershrooms.inboxforreddit3.models.reddit.Message;
import app.endershrooms.inboxforreddit3.models.reddit.RedditAccount;
import app.endershrooms.inboxforreddit3.viewmodels.MessagesActivityViewModel;
import app.endershrooms.inboxforreddit3.views.CustomLinearLayoutManager;
import java.util.concurrent.atomic.AtomicBoolean;

public class ConversationViewFragment extends BaseFragment {

  AtomicBoolean didFirstLoad = new AtomicBoolean(false);
  Toolbar toolbarLayout;

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
    RecyclerView messagesRv = getView().findViewById(R.id.conversation_fragment_messages_list);
    messagesRv.setLayoutManager(new CustomLinearLayoutManager(getContext()));
    ConversationFullAdapter adapter = new ConversationFullAdapter();
    messagesRv.setAdapter(adapter);

    FloatingActionButton fab = getView().findViewById(R.id.conversation_fragment_fab);
    toolbarLayout = getView().findViewById(R.id.conversation_fragment_toolbar);
    toolbarLayout.setOnClickListener(view -> {
      MiscFuncs.smartScrollToTop(messagesRv, 15);
    });

    setToolbarBackButton(toolbarLayout);

    fab.setOnLongClickListener(view -> {
      messagesRv.scrollToPosition((adapter.getItemCount() == 0) ? 0 : adapter.getItemCount() - 1);
      return true;
    });

    model.getAllConversationMessagesPaged(currentAccount, model.getCurrentConversationName().getValue())
    .observe(this, list -> {
      adapter.submitList(list);
      messagesRv.scrollToPosition((adapter.getItemCount() == 0) ? 0 : adapter.getItemCount() - 1);
      if (list != null) {
        onFirstLoad(list.get(0));
      }
    });

    messagesRv.addOnScrollListener(new OnScrollListener() {
      @Override
      public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        if (dy > 0 && dy > 15 && fab.isShown()) {
          fab.hide();
        } else if (dy < 0 && dy < -15 && !fab.isShown()) {
          fab.show();
        }
        super.onScrolled(recyclerView, dx, dy);
      }
    });

  }

  private void onFirstLoad(Message message) {
    if (!didFirstLoad.get()) {
      didFirstLoad.set(true);
      toolbarLayout.setTitle("Conversation with " + message.getCorrespondent());
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    return inflater.inflate(R.layout.fragment_conversation_view, container, false);
  }


}
