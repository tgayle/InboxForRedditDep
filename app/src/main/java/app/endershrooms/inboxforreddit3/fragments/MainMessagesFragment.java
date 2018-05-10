package app.endershrooms.inboxforreddit3.fragments;


import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import app.endershrooms.inboxforreddit3.R;
import app.endershrooms.inboxforreddit3.adapters.MessagesConversationRecyclerViewAdapter;
import app.endershrooms.inboxforreddit3.models.reddit.ResponseWithError;
import app.endershrooms.inboxforreddit3.viewmodels.MessagesActivityViewModel;
import app.endershrooms.inboxforreddit3.viewmodels.MessagesActivityViewModel.LoadingStatusEnum;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MainMessagesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainMessagesFragment extends Fragment {


  MessagesConversationRecyclerViewAdapter messageConversationAdapter;
  SwipeRefreshLayout swipeRefreshLayout;
  RecyclerView messageRv;

  public MainMessagesFragment() {
    // Required empty public constructor
  }

  public static MainMessagesFragment newInstance() {
    MainMessagesFragment fragment = new MainMessagesFragment();
    Bundle args = new Bundle();
    fragment.setArguments(args);
    Log.d("MainMessagesFragment", "MainMessagesFragmentStarted");
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (getArguments() != null) {
    }
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    MessagesActivityViewModel viewModel = ViewModelProviders.of(getActivity()).get(MessagesActivityViewModel.class);
    messageRv = (RecyclerView) getView().findViewById(R.id.message_rv);
    messageRv.setLayoutManager(new LinearLayoutManager(getContext()));
    ((LinearLayoutManager) messageRv.getLayoutManager()).setReverseLayout(true);
    ((LinearLayoutManager) messageRv.getLayoutManager()).setStackFromEnd(true);
    messageConversationAdapter = new MessagesConversationRecyclerViewAdapter(); //reset adapter
    messageRv.setAdapter(messageConversationAdapter);

    swipeRefreshLayout = (SwipeRefreshLayout) getView().findViewById(R.id.activities_messages_swiperefresh);
    View snackbarView = getActivity().findViewById(R.id.messages_activity_fragholder);

    TextView userTv = (TextView) getView().findViewById(R.id.username_tv);

    viewModel.getCurrentAccount().observe(this, redditAccount -> {
      if (redditAccount != null) {
        userTv.setText(String.format(getString(R.string.login_complete_welcome_user), redditAccount.getUsername()));
        Log.d("Messages Fragment", redditAccount.getUsername() + " account is " + ((redditAccount.getAccountIsNew()) ? "new" : "not new"));

        swipeRefreshLayout.setOnRefreshListener(() -> {
          startRefresh(viewModel);
        });

        viewModel.getMessagesForConversationView().observe(MainMessagesFragment.this, conversations -> {
          messageConversationAdapter.submitList(conversations);
        });

        if (redditAccount.getAccountIsNew()) { //Load all messages if new
          LiveData<ResponseWithError<String, Throwable>> messagesStatus = viewModel.loadAllMessages();
          viewModel.setAccountIsNew(false);
          Snackbar loading = Snackbar.make(snackbarView, "Loading...", Snackbar.LENGTH_INDEFINITE);
          loading.show();
          messagesStatus.observe(this, response -> {
            if (response != null) {
              if (response.getError() == null) {
                String afterResult = response.getData();
                String text = "After is " + afterResult;
                loading.setText(text);
                Log.d("MessagesFragment", text);
                if (afterResult == null) {
                  loading.dismiss();
                  viewModel.setLoadingStatus(LoadingStatusEnum.DONE);
                }
              } else {
                viewModel.setLoadingStatus(LoadingStatusEnum.ERROR, response.getError());
              }
            }
          });
        } else {
          startRefresh(viewModel);
        }

        Snackbar errorSnack = Snackbar.make(snackbarView,
            "There was an issue...", Snackbar.LENGTH_INDEFINITE);

        viewModel.getLoadingStatus().observe(this, newStatus -> {
          if (newStatus != null) {
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
    });
  }

  void startRefresh(MessagesActivityViewModel viewModel) {
    viewModel.setLoadingStatus(LoadingStatusEnum.LOADING);

    viewModel.loadNewestMessages().observe(this, stringThrowableResponseWithError -> {
      if (stringThrowableResponseWithError != null) {
        if (stringThrowableResponseWithError.getData() == null && getView() != null) {
          viewModel.setLoadingStatus(LoadingStatusEnum.DONE);
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

    //TODO: Expand and view conversation
    //TODO: Replying to messages
    //TODO: Message notifications
    //TODO: Viewing images and webpages in here.
    //TODO: Revoke token on account remove.
    //TODO: Get new client id for security reasons.

    return inflater.inflate(R.layout.activity_messages_fragment, container,false);
  }
}
