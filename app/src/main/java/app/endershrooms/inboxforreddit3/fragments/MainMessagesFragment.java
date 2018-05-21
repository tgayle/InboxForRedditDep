package app.endershrooms.inboxforreddit3.fragments;


import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import app.endershrooms.inboxforreddit3.R;
import app.endershrooms.inboxforreddit3.adapters.ConversationPreviewAdapter;
import app.endershrooms.inboxforreddit3.models.reddit.RedditAccount;
import app.endershrooms.inboxforreddit3.models.reddit.ResponseWithError;
import app.endershrooms.inboxforreddit3.viewmodels.MessagesActivityViewModel;
import app.endershrooms.inboxforreddit3.viewmodels.MessagesActivityViewModel.LoadingStatusEnum;
import app.endershrooms.inboxforreddit3.views.CustomLinearLayoutManager;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MainMessagesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainMessagesFragment extends Fragment {


  ConversationPreviewAdapter messageConversationAdapter;
  SwipeRefreshLayout swipeRefreshLayout;
  RecyclerView messageRv;

  public MainMessagesFragment() {
    // Required empty public constructor
  }

  public static MainMessagesFragment newInstance() {
    return new MainMessagesFragment();
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    MessagesActivityViewModel viewModel = ViewModelProviders.of(getActivity()).get(MessagesActivityViewModel.class);
    RedditAccount currentAccount = viewModel.getCurrentAccount().getValue();

    messageRv = (RecyclerView) getView().findViewById(R.id.message_rv);
    CustomLinearLayoutManager linearLayoutManager = new CustomLinearLayoutManager(getContext());
    messageRv.setLayoutManager(linearLayoutManager);
    linearLayoutManager.setReverseLayout(true);
    linearLayoutManager.setStackFromEnd(true);
    messageConversationAdapter = new ConversationPreviewAdapter(
        message -> viewModel.setCurrentConversationName(message.getParentMessageName())); //reset adapter
    messageRv.setAdapter(messageConversationAdapter);

    swipeRefreshLayout = (SwipeRefreshLayout) getView().findViewById(R.id.activities_messages_swiperefresh);
    View snackbarView = getActivity().findViewById(R.id.messages_activity_fragholder);
    TextView userTv = (TextView) getView().findViewById(R.id.username_tv);

    userTv.setText(String.format(getString(R.string.login_complete_welcome_user), currentAccount.getUsername()));
    Log.d("Messages Fragment", currentAccount.getUsername() + " account is " + ((currentAccount.getAccountIsNew()) ? "new" : "not new"));

    prepareLoadingStatus(viewModel);

    swipeRefreshLayout.setOnRefreshListener(() -> {
      startRefresh(viewModel);
    });

    viewModel.getMessagesForConversationView().observe(MainMessagesFragment.this, conversations -> {
      messageConversationAdapter.submitList(conversations);
      //Scroll to top when we update the list.
      if (messageConversationAdapter.getItemCount() != 0) {
        messageRv.scrollToPosition(messageConversationAdapter.getItemCount() - 1);
      }
    });
    Snackbar loading = Snackbar.make(snackbarView, "Loading...", Snackbar.LENGTH_INDEFINITE);
    if (currentAccount.getAccountIsNew()) { //Load all messages if new
      LiveData<ResponseWithError<String, Throwable>> messagesStatus = viewModel.loadAllMessages();
      viewModel.setAccountIsNew(false);
    } else {
      ResponseWithError<LoadingStatusEnum, String> currentStatus = viewModel.getLoadingStatus().getValue();
      if (currentStatus != null && currentStatus.getData() != LoadingStatusEnum.LOADING) {
        startRefresh(viewModel);
      }
    }
  }

  void startRefresh(MessagesActivityViewModel viewModel) {
    viewModel.setLoadingStatus(LoadingStatusEnum.LOADING);

    viewModel.loadNewestMessages().observe(this, stringThrowableResponseWithError -> {
      if (stringThrowableResponseWithError != null) {
        if (stringThrowableResponseWithError.getData() == null && getView() != null) {
          Log.d("LoadingStatus", "LoadNewest is " + stringThrowableResponseWithError.getData());
          viewModel.setLoadingStatus(LoadingStatusEnum.DONE);
        }
      }
    });
  }

  void prepareLoadingStatus(MessagesActivityViewModel viewModel) {
    Snackbar errorSnack = Snackbar.make( getActivity().findViewById(R.id.messages_activity_fragholder),
        "There was an issue...", Snackbar.LENGTH_INDEFINITE);

    viewModel.getLoadingStatus().observe(this, newStatus -> {
      if (getView() != null && newStatus != null) {
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
              createAlertDialog(newStatus.getError()).show();
            });
//            loading.dismiss();
            errorSnack.show();
            break;
          case DONE:
            swipeRefreshLayout.setRefreshing(false);
            errorSnack.dismiss();
//            loading.dismiss();
            break;
        }
      }
    });
  }

  public AlertDialog createAlertDialog(String message) {
    return new AlertDialog.Builder(getContext())
        .setMessage(message)
        .setTitle("Error")
        .create();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {

    //TODO: Replying to messages
    //TODO: Marking messages as read on open or reply
    //TODO: Message notifications
    //TODO: Viewing images and webpages in here.

    return inflater.inflate(R.layout.activity_messages_fragment, container,false);
  }
}
