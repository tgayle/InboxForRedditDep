package app.endershrooms.inboxforreddit3.conductor;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AlertDialog.Builder;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import app.endershrooms.inboxforreddit3.MiscFuncs;
import app.endershrooms.inboxforreddit3.R;
import app.endershrooms.inboxforreddit3.adapters.ConversationPreviewAdapter;
import app.endershrooms.inboxforreddit3.viewmodels.MessagesActivityViewModel;
import app.endershrooms.inboxforreddit3.viewmodels.MessagesActivityViewModel.LoadingStatusEnum;
import app.endershrooms.inboxforreddit3.viewmodels.model.MessagesActivityDataModel;
import app.endershrooms.inboxforreddit3.views.CustomLinearLayoutManager;
import app.endershrooms.inboxforreddit3.views.UnreadMessageButtonView;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.changehandler.HorizontalChangeHandler;

public class MainMessagesController extends LifecycleActivityController {
  private ConversationPreviewAdapter messageConversationAdapter;
  private SwipeRefreshLayout swipeRefreshLayout;
  private RecyclerView messageRv;
  private UnreadMessageButtonView unreadMessageButtonView;
  private Toolbar toolbar;

  private MessagesActivityViewModel viewModel;

  public MainMessagesController() {
    setRetainViewMode(RetainViewMode.RETAIN_DETACH);
  }

  @NonNull
  @Override
  protected View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    //TODO: Replying to messages
    //TODO: Marking messages as read on open or reply
    //TODO: Message notifications
    //TODO: Viewing images and webpages in here.
    //TODO: Hide/Delete messages locally.

    View view = inflater.inflate(R.layout.activity_messages_fragment, container,false);
    viewModel = ViewModelProviders.of(getLifecycleActivity()).get(MessagesActivityViewModel.class);
    MessagesActivityDataModel dataModel = viewModel.getDataModel();

    //Setup views
    prepareViews(view);
    prepareLogic(view);

    messageConversationAdapter = new ConversationPreviewAdapter(message -> {
      getRouter()
          .pushController(RouterTransaction.with(new ConversationViewController(message.getParentMessageName()))
          .pushChangeHandler(new HorizontalChangeHandler())
          .popChangeHandler(new HorizontalChangeHandler()));
    });
    messageRv.setAdapter(messageConversationAdapter);


    dataModel.getUnreadMessagesAsList().observe(this, messages -> {
      if (messages != null) {
        unreadMessageButtonView.setUnreadMessages(messages.size());
      } else {
        unreadMessageButtonView.setUnreadMessages(null);
      }
    });

    dataModel.getMessagesForConversationView().observe(this, conversations -> {
      messageConversationAdapter.submitList(conversations);
      Log.d("GetMessageConvo", "Size: " + (conversations != null ? conversations.size() : conversations));
      //Scroll to top when we update the list.
      scrollRecyclerViewToTop(messageRv);
    });

    dataModel.getCurrentAccount().observe(this, currentAccount -> {
      if (currentAccount != null) {
        scrollRecyclerViewToTop(messageRv);
        if (currentAccount.getAccountIsNew()) { //Load all messages if new
          viewModel.loadAllMessages();
          viewModel.setAccountIsNew(false);
        } else {
          Log.d("MainMessages", "Starting refresh");
          startRefresh();
        }
      }
    });
    return view;
  }

  private void prepareViews(View rootView) {
    swipeRefreshLayout = rootView.findViewById(R.id.activities_messages_swiperefresh);
    messageRv = rootView.findViewById(R.id.messages_frag_message_rv);
    CustomLinearLayoutManager linearLayoutManager = new CustomLinearLayoutManager(rootView.getContext());
    messageRv.setLayoutManager(linearLayoutManager);
    linearLayoutManager.setReverseLayout(true);
    linearLayoutManager.setStackFromEnd(true);

    toolbar = rootView.findViewById(R.id.main_messages_frag_toolbar);
    toolbar.setTitle("Messages");

    unreadMessageButtonView = toolbar.findViewById(R.id.messages_fragment_toolbar_unreadmsgs_view);
    unreadMessageButtonView.hide();
  }

  private void prepareLogic(View rootView) {
    swipeRefreshLayout.setOnRefreshListener(this::startRefresh);
    prepareLoadingStatus(rootView);

    toolbar.setOnClickListener(view -> MiscFuncs.smartScrollToTop(messageRv, 15));

    unreadMessageButtonView.setOnClickListener(view -> {
      new Builder(rootView.getContext())
          .setTitle("Mark all messages as read?")
          .setPositiveButton("Confirm",
              (dialogInterface, i) -> {
                Snackbar clearing = Snackbar.make(rootView, "Clearing messages...", Snackbar.LENGTH_LONG);
                clearing.show();
                Snackbar finished = Snackbar.make(rootView, "All messages marked as read!", Snackbar.LENGTH_SHORT);
                viewModel.markAllMessagesAsRead().observe(this, isFinished -> {
                  if (isFinished != null && isFinished) {
                    clearing.dismiss();
                    finished.show();
                  }
                });
              })
          .setNegativeButton("Cancel",
              (dialogInterface, i) -> {
                Toast.makeText(rootView.getContext(), "Cancelled", Toast.LENGTH_SHORT).show();
              })
          .show();
    });

  }

  private void startRefresh() {
    viewModel.setLoadingStatus(LoadingStatusEnum.LOADING);

    viewModel.loadNewestMessages().observe(this, stringThrowableResponseWithError -> {
      if (stringThrowableResponseWithError != null) {
        if ((stringThrowableResponseWithError.getData() == null || stringThrowableResponseWithError.getData().equals(""))) {
          Log.d("LoadingStatus", "LoadNewest is " + stringThrowableResponseWithError.getData());
          viewModel.setLoadingStatus(LoadingStatusEnum.DONE);
        }
      }
    });
  }

  private void prepareLoadingStatus(View rootView) {
    Snackbar errorSnack = Snackbar.make( rootView,
        "There was an issue...", Snackbar.LENGTH_INDEFINITE);

    viewModel.getLoadingStatus().observe(this, newStatus -> {
      if (newStatus != null) {
        Log.d("LoadingStatus", "Current status is " + newStatus.getData());
        errorSnack.setAction(null, null); //reset in case it changed
        switch (newStatus.getData()) {
          case LOADING:
            swipeRefreshLayout.setRefreshing(true);
            errorSnack.dismiss();
            break;
          case ERROR:
            swipeRefreshLayout.setRefreshing(false);
            errorSnack.setAction("View", view -> {
              createAlertDialog(rootView.getContext(), newStatus.getError(), null).show();
            });
            errorSnack.show();
            break;
          case DONE:
            swipeRefreshLayout.setRefreshing(false);
            errorSnack.dismiss();
            break;
        }
      }
    });
  }

  private void scrollRecyclerViewToTop(RecyclerView rv) {
    if (messageConversationAdapter != null && messageConversationAdapter.getItemCount() != 0) {
      rv.scrollToPosition(messageConversationAdapter.getItemCount() - 1);
    }
  }

  private AlertDialog createAlertDialog(Context context, String message, @Nullable AlertDialog.OnClickListener onClickListener) {
    return new AlertDialog.Builder(context)
        .setMessage(message)
        .setTitle("Error")
        .setPositiveButton("Confirm", onClickListener)
        .create();
  }


}
