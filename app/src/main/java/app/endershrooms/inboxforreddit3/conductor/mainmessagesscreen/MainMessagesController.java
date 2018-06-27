package app.endershrooms.inboxforreddit3.conductor.mainmessagesscreen;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AlertDialog.Builder;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Toast;
import app.endershrooms.inboxforreddit3.MiscFuncs;
import app.endershrooms.inboxforreddit3.R;
import app.endershrooms.inboxforreddit3.adapters.ConversationPreviewAdapter;
import app.endershrooms.inboxforreddit3.adapters.ConversationPreviewAdapter.PreviewViewHolder;
import app.endershrooms.inboxforreddit3.conductor.LifecycleActivityController;
import app.endershrooms.inboxforreddit3.conductor.conversationview.ConversationViewController;
import app.endershrooms.inboxforreddit3.interfaces.OnMessageSelectedInterface;
import app.endershrooms.inboxforreddit3.models.reddit.Message;
import app.endershrooms.inboxforreddit3.viewmodels.MessagesActivityViewModel;
import app.endershrooms.inboxforreddit3.viewmodels.MessagesActivityViewModel.LoadingStatusEnum;
import app.endershrooms.inboxforreddit3.viewmodels.MessagesActivityViewModelFactory;
import app.endershrooms.inboxforreddit3.viewmodels.model.MessagesActivityDataModel;
import app.endershrooms.inboxforreddit3.viewmodels.model.MessagesControllerViewModel;
import app.endershrooms.inboxforreddit3.views.CustomLinearLayoutManager;
import app.endershrooms.inboxforreddit3.views.UnreadMessageButtonView;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.changehandler.HorizontalChangeHandler;

public class MainMessagesController extends LifecycleActivityController {
  private ConversationPreviewAdapter messageConversationAdapter;
  private SwipeRefreshLayout swipeRefreshLayout;
  private RecyclerView messageRv;

  private MessagesActivityViewModel activityViewModel;
  private MessagesActivityDataModel dataModel;
  private MessagesControllerViewModel controllerViewModel;

  private Toolbar toolbar;

  public MainMessagesController() {
    setRetainViewMode(RetainViewMode.RETAIN_DETACH);
    setHasOptionsMenu(true);
  }

  @NonNull
  @Override
  protected View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    //TODO: Replying to messages
    //TODO: Marking messages as read on open or reply
    //TODO: Message notifications
    //TODO: Viewing images and webpages in here.
    //TODO: Hide/Delete messages locally.

    toolbar = getLifecycleActivity().findViewById(R.id.messages_activity_toolbar);
    activityViewModel = ViewModelProviders.of(getLifecycleActivity()).get(MessagesActivityViewModel.class);
    View view = inflater.inflate(R.layout.activity_messages_fragment, container,false);
    dataModel = activityViewModel.getDataModel();
    controllerViewModel = new MessagesActivityViewModelFactory(dataModel).create(MessagesControllerViewModel.class);

    //Setup views
    prepareViews(view);
    prepareLogic(view);

    messageConversationAdapter = new ConversationPreviewAdapter(new OnMessageSelectedInterface() {
      @Override
      public void onMessageSelected(Message message) {
        MainMessagesController.this.getRouter()
            .pushController(RouterTransaction
                .with(new ConversationViewController(message.getParentMessageName()))
                .pushChangeHandler(new HorizontalChangeHandler())
                .popChangeHandler(new HorizontalChangeHandler()));
      }

      @Override
      public void onMessageLongSelect(ViewHolder previewViewHolder, Message message) {
        getLifecycleActivity().startActionMode(new MessageSelectedActionMode<>(getView().getContext(),
            messageConversationAdapter, itemsToDelete -> {
            controllerViewModel.deleteMessages(itemsToDelete);
            return null;
        }, () -> {
          swipeRefreshLayout.setEnabled(false);
          messageConversationAdapter
              .markItemSelected((PreviewViewHolder) previewViewHolder, message);
          return null;
        }, () -> {
          swipeRefreshLayout.setEnabled(true);
          return null;
        }));
      }
    });
    messageRv.setAdapter(messageConversationAdapter);

    dataModel.getMessagesInInboxForConversationView().observe(this, conversations -> {
      messageConversationAdapter.submitList(conversations);
      Log.d("GetMessageConvo", "Size: " + (conversations != null ? conversations.size() : conversations));
      //Scroll to top when we update the list.
      scrollRecyclerViewToTop(messageRv);
    });

    dataModel.getCurrentAccount().observe(this, currentAccount -> {
      if (currentAccount != null) {
        scrollRecyclerViewToTop(messageRv);
        if (currentAccount.getAccountIsNew()) { //Load all messages if isNew
          controllerViewModel.loadAllMessages();
          activityViewModel.setAccountIsNew(false);
        } else {
          Log.d("MainMessages", "Starting refresh");
          startRefresh();
        }
      }
    });
    return view;
  }

  @Override
  protected void onAttach(@NonNull View view) {
    super.onAttach(view);
    getLifecycleActivity().setTitle("Messages");
    toolbar.setOnClickListener(toolbarView -> MiscFuncs.smartScrollToTop(messageRv, 15));
  }

  private void prepareViews(View rootView) {
    swipeRefreshLayout = rootView.findViewById(R.id.activities_messages_swiperefresh);
    messageRv = rootView.findViewById(R.id.messages_frag_message_rv);
    CustomLinearLayoutManager linearLayoutManager = new CustomLinearLayoutManager(rootView.getContext());
    messageRv.setLayoutManager(linearLayoutManager);
    linearLayoutManager.setReverseLayout(true);
    linearLayoutManager.setStackFromEnd(true);

  }

  private void prepareLogic(View rootView) {
    swipeRefreshLayout.setOnRefreshListener(this::startRefresh);
    prepareLoadingStatus(rootView);
  }

  private void startRefresh() {
    controllerViewModel.setLoadingStatus(LoadingStatusEnum.LOADING);

    controllerViewModel.loadNewestMessages().observe(this, stringThrowableResponseWithError -> {
      if (stringThrowableResponseWithError != null) {
        if ((stringThrowableResponseWithError.getData() == null || stringThrowableResponseWithError.getData().equals(""))) {
          Log.d("LoadingStatus", "LoadNewest is " + stringThrowableResponseWithError.getData());
          controllerViewModel.setLoadingStatus(LoadingStatusEnum.DONE);
        }
      }
    });
  }

  private void prepareLoadingStatus(View rootView) {
    Snackbar errorSnack = Snackbar.make( rootView,
        "There was an issue...", Snackbar.LENGTH_INDEFINITE);

    controllerViewModel.getLoadingStatus().observe(this, newStatus -> {
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

  private View.OnClickListener onUnreadToolbarBtnClicked = new OnClickListener() {
    @Override
    public void onClick(View view) {
      new Builder(getView().getContext())
          .setTitle("Mark all messages as read?")
          .setPositiveButton("Confirm",
              (dialogInterface, i) -> {
                Snackbar clearing = Snackbar.make(getView(), "Clearing messages...", Snackbar.LENGTH_LONG);
                clearing.show();
                Snackbar finished = Snackbar.make(getView(), "All messages marked as read!", Snackbar.LENGTH_SHORT);
                controllerViewModel
                    .markAllMessagesAsRead().observe(MainMessagesController.this, isFinished -> {
                  if (isFinished != null && isFinished) {
                    clearing.dismiss();
                    finished.show();
                  }
                });
              })
          .setNegativeButton("Cancel",
              (dialogInterface, i) -> {
                Toast.makeText(view.getContext(), "Cancelled", Toast.LENGTH_SHORT).show();
              })
          .show();
    }
  };

  @Override
  public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    inflater.inflate(R.menu.main_messages_controller_toolbar, menu);
    UnreadMessageButtonView unreadMessageButtonView = (UnreadMessageButtonView) menu.findItem(R.id.unread_messages_btn_info).getActionView();
    unreadMessageButtonView.setOnClickListener(onUnreadToolbarBtnClicked);

    dataModel.getUnreadMessagesAsList().observe(this, messages -> {
      if (messages != null) {
        unreadMessageButtonView.setUnreadMessages(messages.size());
      } else {
        unreadMessageButtonView.setUnreadMessages(null);
      }
    });
  }

  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    switch (item.getItemId()) {
      case R.id.main_messages_controller_menu_refresh:
        startRefresh();
        return true;
      case R.id.force_load_all_messages:
        controllerViewModel.loadAllMessages();
        return true;
      default:
        return false;
    }
  }
}
