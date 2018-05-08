package app.endershrooms.inboxforreddit3.fragments;


import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import app.endershrooms.inboxforreddit3.R;
import app.endershrooms.inboxforreddit3.Singleton;
import app.endershrooms.inboxforreddit3.adapters.MessagesConversationRecyclerViewAdapter;
import app.endershrooms.inboxforreddit3.models.reddit.Conversation;
import app.endershrooms.inboxforreddit3.models.reddit.RedditAccount;
import app.endershrooms.inboxforreddit3.net.APIManager;
import app.endershrooms.inboxforreddit3.viewmodels.MessagesActivityViewModel;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MainMessagesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainMessagesFragment extends Fragment {


  MessagesConversationRecyclerViewAdapter messageConversationAdapter = new MessagesConversationRecyclerViewAdapter();
  SwipeRefreshLayout swipeRefreshLayout;
  RecyclerView messageRv;

  public MainMessagesFragment() {
    // Required empty public constructor
  }

  public static MainMessagesFragment newInstance() {
    MainMessagesFragment fragment = new MainMessagesFragment();
    Bundle args = new Bundle();
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (getArguments() != null) {
    }
  }

  Snackbar snack = Snackbar.make(getActivity().findViewById(R.id.messages_activity_fragholder), "Loading messages...", Snackbar.LENGTH_INDEFINITE);

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    MessagesActivityViewModel viewModel = ViewModelProviders.of(getActivity()).get(MessagesActivityViewModel.class);

    messageRv = (RecyclerView) getView().findViewById(R.id.message_rv);
    messageRv.setLayoutManager(new LinearLayoutManager(getContext()));
    messageRv.setAdapter(messageConversationAdapter);
    ((LinearLayoutManager) messageRv.getLayoutManager()).setReverseLayout(true);
    ((LinearLayoutManager) messageRv.getLayoutManager()).setStackFromEnd(true);

    swipeRefreshLayout = (SwipeRefreshLayout) getView().findViewById(R.id.activities_messages_swiperefresh);

    TextView userTv = (TextView) getView().findViewById(R.id.username_tv);

    viewModel.getCurrentAccount().observe(this, redditAccount -> {
      if (redditAccount != null) {
        userTv.setText(String.format(getString(R.string.login_complete_welcome_user), redditAccount.getUsername()));
        Log.d("Messages Fragment", redditAccount.getUsername() + " account is " + ((redditAccount.getAccountIsNew()) ? "new" : "not new"));

        swipeRefreshLayout.setOnRefreshListener(() -> {
          updateMessagesAndView(redditAccount);
        });
        if (redditAccount.getAccountIsNew()) {
          loadAllPastMessages(redditAccount);
        } else {

          viewModel.getMessagesForConversationView().observe(MainMessagesFragment.this, conversations -> {
            messageConversationAdapter.submitList(conversations);
          });
          //Load messages in paged view?
        }
        Snackbar errorSnack = Snackbar.make(getActivity().findViewById(R.id.messages_activity_fragholder),
            "There was an issue...", Snackbar.LENGTH_INDEFINITE);

        viewModel.getLoadingStatus().observe(this, newStatus -> {
          if (newStatus != null) {
            switch (newStatus.getData()) {
              case LOADING:
                swipeRefreshLayout.setRefreshing(true);
                errorSnack.dismiss();
                break;
              case ERROR:
                swipeRefreshLayout.setRefreshing(false);
                errorSnack.show();
                break;
            }
          }
        });
      }
    });
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {

    //TODO: Expand and view conversation
    //TODO: Replying to messages
    //TODO: Message notifications
    //TODO: Viewing images and webpages in here.

    return inflater.inflate(R.layout.activity_messages_fragment, container,false);
  }

  void loadAllPastMessages(RedditAccount currentUser) {
    swipeRefreshLayout.setRefreshing(true);
    snack.show();
    APIManager.get().updateUserToken(currentUser, () -> {
      APIManager.get().downloadAllPastMessages(currentUser, "inbox", 20, "", (beforOrAfter, after, messagesLoaded) -> {
        if (after == null) {
          System.out.println("Download ended from Activity: after = " + after + " and messagesLoaded was " + messagesLoaded);
          APIManager.get().downloadAllPastMessages(currentUser, "sent", 20, "", ((beforeOrAfter, sentAfter, numAfterLoaded) -> {
            if (sentAfter == null){
              System.out.println("Download sent ended from Activity: after = " + sentAfter + " and messagesLoaded was " + messagesLoaded);
              Singleton.get().getDb().messages().getAllUserMessagesAsc(currentUser.getUsername())
                  .subscribeOn(Schedulers.io())
                  .observeOn(Schedulers.computation())
                  .map(Conversation::formConversations)
                  .observeOn(AndroidSchedulers.mainThread())
                  .subscribe(conversations -> {
                    swipeRefreshLayout.setRefreshing(false);
                    currentUser.setAccountIsNew(false);
                    Single.fromCallable(() -> Singleton.get().getDb().accounts().updateAccount(currentUser))
                        .subscribeOn(Schedulers.io())
                        .subscribe();
                  });
            }
          }), throwable -> {

          });
        }
      }, throwable -> {
        Snackbar.make(getActivity().findViewById(R.id.messages_activity_fragholder), "Issue loading messages " + "loadAllPastMessages()", Snackbar.LENGTH_SHORT).show();
        swipeRefreshLayout.setRefreshing(false);
      });
    }, tokenError -> {
      Snackbar.make(getActivity().findViewById(R.id.messages_activity_fragholder), "Error updating token", Snackbar.LENGTH_LONG).setAction("Log Error", (view) -> {
        System.out.println(tokenError);
      }).show();
    });
  }

  void updateMessagesAndView(RedditAccount currentUser) {
    swipeRefreshLayout.setRefreshing(true);
    APIManager.get().updateUserToken(currentUser, () -> {
      Singleton.get().getDb().messages().getNewestMessageInDatabase(currentUser.getUsername())
          .subscribeOn(Schedulers.io())
          .subscribe(newestMsg-> {
            APIManager.get().downloadAllFutureMessages(currentUser, "inbox", 15, newestMsg.getMessageName(),
                (beforeOrAfter, after, messagesLoaded) -> {
                  if (after == null) {
                    System.out.println("From activity: beforeOrAfter: " + beforeOrAfter + " pager is " + after + " messagesLoaded is " + messagesLoaded);
                    Singleton.get().getDb().messages().getAllUserMessagesAsc(currentUser.getUsername())
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.computation())
                        .map(Conversation::formConversations)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(conversations -> {
                          swipeRefreshLayout.setRefreshing(false);
                        });
                  }

                }, err -> {
                  Snackbar.make(getActivity().findViewById(R.id.messages_activity_fragholder), "There was an issue loading messages. " + err , Snackbar.LENGTH_SHORT).show();
                });
          }, (err) -> {
            System.out.println("There was an issue loading the newest message : " + err);
          });

    }, tokenError -> {
      Snackbar.make(getActivity().findViewById(R.id.messages_activity_fragholder), "Error updating token", Snackbar.LENGTH_LONG).setAction("Log Error", (view) -> {
        System.out.println(tokenError);
      }).show();
    });
  }

}
