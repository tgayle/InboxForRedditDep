package app.endershrooms.inboxforreddit3.conductor;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import app.endershrooms.inboxforreddit3.BundleCreator;
import app.endershrooms.inboxforreddit3.MiscFuncs;
import app.endershrooms.inboxforreddit3.R;
import app.endershrooms.inboxforreddit3.adapters.ConversationFullAdapter;
import app.endershrooms.inboxforreddit3.models.reddit.Message;
import app.endershrooms.inboxforreddit3.models.reddit.RedditAccount;
import app.endershrooms.inboxforreddit3.viewmodels.MessagesActivityViewModel;
import app.endershrooms.inboxforreddit3.viewmodels.model.MessagesActivityDataModel;
import app.endershrooms.inboxforreddit3.views.CustomLinearLayoutManager;
import java.util.concurrent.atomic.AtomicBoolean;

class ConversationViewController extends LifecycleActivityController {

  private AtomicBoolean didFirstLoad = new AtomicBoolean(false);
  private Toolbar toolbarLayout;
  private String parentMessageName;
  public static final String MESSAGE_PARENT_NAME_KEY = "MESSAGE_PARENT_NAME_KEY";


  public ConversationViewController(String parentMessageName) {
    this(new BundleCreator(new Bundle())
    .putString(MESSAGE_PARENT_NAME_KEY, parentMessageName)
        .build());
  }

  public ConversationViewController(Bundle args) {
    this.parentMessageName = args.getString(MESSAGE_PARENT_NAME_KEY);
  }

  @Override
  public void onAttach(View rootView) {
    MessagesActivityViewModel activityModel = ViewModelProviders.of(getLifecycleActivity()).get(MessagesActivityViewModel.class);
    MessagesActivityDataModel dataModel = activityModel.getDataModel();

    RedditAccount currentAccount = dataModel.getCurrentAccount().getValue();
    Log.d("ConversationViewFrag", "Current account is " + currentAccount);
    RecyclerView messagesRv = getView().findViewById(R.id.conversation_fragment_messages_list);
    messagesRv.setLayoutManager(new CustomLinearLayoutManager(rootView.getContext()));
    ConversationFullAdapter adapter = new ConversationFullAdapter();
    messagesRv.setAdapter(adapter);

    FloatingActionButton fab = getView().findViewById(R.id.conversation_fragment_fab);
    toolbarLayout = getView().findViewById(R.id.conversation_fragment_toolbar);
    toolbarLayout.setOnClickListener(view -> {
      MiscFuncs.smartScrollToTop(messagesRv, 15);
    });

    toolbarLayout.setNavigationIcon(rootView.getContext().getResources().getDrawable(R.drawable.ic_action_back));
    toolbarLayout.setNavigationOnClickListener(view -> getRouter().popCurrentController());

    fab.setOnLongClickListener(view -> {
      messagesRv.scrollToPosition((adapter.getItemCount() == 0) ? 0 : adapter.getItemCount() - 1);
      return true;
    });

    dataModel.getAllConversationMessagesPaged(currentAccount, parentMessageName)
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
  @NonNull
  public View onCreateView(LayoutInflater inflater, ViewGroup container) {
    // Inflate the layout for this fragment
    return inflater.inflate(R.layout.fragment_conversation_view, container, false);
  }


}
