package app.endershrooms.inboxforreddit3.fragments;


import android.arch.lifecycle.ViewModelProviders;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import app.endershrooms.inboxforreddit3.MiscFuncs;
import app.endershrooms.inboxforreddit3.R;
import app.endershrooms.inboxforreddit3.adapters.ConversationPreviewAdapter;
import app.endershrooms.inboxforreddit3.models.reddit.RedditAccount;
import app.endershrooms.inboxforreddit3.viewmodels.MessagesActivityViewModel;
import app.endershrooms.inboxforreddit3.viewmodels.MessagesActivityViewModel.LoadingStatusEnum;
import app.endershrooms.inboxforreddit3.views.CustomLinearLayoutManager;

public class MainMessagesFragment extends BaseFragment {


  ConversationPreviewAdapter messageConversationAdapter;
  SwipeRefreshLayout swipeRefreshLayout;
  RecyclerView messageRv;

  MessagesActivityViewModel viewModel;


  public MainMessagesFragment() {
    // Required empty public constructor
  }

  public static MainMessagesFragment newInstance() {
    return new MainMessagesFragment();
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    viewModel = ViewModelProviders.of(getActivity()).get(MessagesActivityViewModel.class);
    RedditAccount currentAccount = viewModel.getCurrentAccount().getValue();

    //Setup views
    messageRv = (RecyclerView) getView().findViewById(R.id.messages_frag_message_rv);
    CustomLinearLayoutManager linearLayoutManager = new CustomLinearLayoutManager(getContext());
    messageRv.setLayoutManager(linearLayoutManager);
    linearLayoutManager.setReverseLayout(true);
    linearLayoutManager.setStackFromEnd(true);
    messageConversationAdapter = new ConversationPreviewAdapter(
        message -> viewModel.setCurrentConversationName(message.getParentMessageName())); //reset adapter
    messageRv.setAdapter(messageConversationAdapter);

    swipeRefreshLayout = (SwipeRefreshLayout) getView().findViewById(R.id.activities_messages_swiperefresh);
    View snackbarView = getActivity().findViewById(R.id.messages_activity_fragholder);

    Toolbar toolbar = getView().findViewById(R.id.main_messages_frag_toolbar);
    toolbar.setTitle("Messages");
    toolbar.setOnClickListener(view -> {
      MiscFuncs.smartScrollToTop(messageRv, 15);
    });

    setToolbarBackButton(toolbar, new OnClickListener() {
      @Override
      public void onClick(View view) {
        //TODO:Open drawer.
      }
    });

    //Logic
    prepareLoadingStatus();

    swipeRefreshLayout.setOnRefreshListener(() -> startRefresh());

    viewModel.getMessagesForConversationView().observe(MainMessagesFragment.this, conversations -> {
      messageConversationAdapter.submitList(conversations);
      //Scroll to top when we update the list.
      if (messageConversationAdapter.getItemCount() != 0) {
        messageRv.scrollToPosition(messageConversationAdapter.getItemCount() - 1);
      }
    });

    if (currentAccount.getAccountIsNew()) { //Load all messages if new
      viewModel.loadAllMessages();
      viewModel.setAccountIsNew(false);
    } else {
      Log.d("MainMessages", "Starting refresh");
      startRefresh();
    }
  }

  void startRefresh() {
    viewModel.setLoadingStatus(LoadingStatusEnum.LOADING);

    viewModel.loadNewestMessages().observe(this, stringThrowableResponseWithError -> {
      if (stringThrowableResponseWithError != null) {
        if ((stringThrowableResponseWithError.getData() == null || stringThrowableResponseWithError.getData().equals("")) && getView() != null) {
          Log.d("LoadingStatus", "LoadNewest is " + stringThrowableResponseWithError.getData());
          viewModel.setLoadingStatus(LoadingStatusEnum.DONE);
        }
      }
    });
  }

  void prepareLoadingStatus() {
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
              createAlertDialog(newStatus.getError(), null).show();
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

  void setActionbarDrawerCommunication(DrawerLayout drawer, Toolbar toolbar) {
    Log.d("MessagesFrag", "Actionbar set up");
    ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(getActivity(), drawer, toolbar, R.string.drawer_open, R.string.drawer_close);
    drawer.addDrawerListener(actionBarDrawerToggle);
    actionBarDrawerToggle.getDrawerArrowDrawable().setColor(Color.WHITE);
    actionBarDrawerToggle.syncState();
  }

  private AlertDialog createAlertDialog(String message, @Nullable AlertDialog.OnClickListener onClickListener) {
    return new AlertDialog.Builder(getContext())
        .setMessage(message)
        .setTitle("Error")
        .setPositiveButton("Confirm", onClickListener)
        .create();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {

    //TODO: List number of unread messages.
    //TODO: Replying to messages
    //TODO: Marking messages as read on open or reply
    //TODO: Message notifications
    //TODO: Viewing images and webpages in here.

    return inflater.inflate(R.layout.activity_messages_fragment, container,false);
  }
}
