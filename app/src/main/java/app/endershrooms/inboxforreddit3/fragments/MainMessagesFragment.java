package app.endershrooms.inboxforreddit3.fragments;


import android.os.Bundle;
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
import app.endershrooms.inboxforreddit3.models.Conversation;
import app.endershrooms.inboxforreddit3.models.Message;
import app.endershrooms.inboxforreddit3.models.RedditAccount;
import app.endershrooms.inboxforreddit3.net.APIManager;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MainMessagesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainMessagesFragment extends Fragment {


  MessagesConversationRecyclerViewAdapter messageConversationAdapter = new MessagesConversationRecyclerViewAdapter(new ArrayList<>());
  SwipeRefreshLayout swipeRefreshLayout;
  RecyclerView messageRv;
  RedditAccount currentUser;

  public MainMessagesFragment() {
    // Required empty public constructor
  }

  /**
   * Use this factory method to create a new instance of
   * this fragment using the provided parameters.
   *
   * @return A new instance of fragment MainMessagesFragment.
   */
  public static MainMessagesFragment newInstance(RedditAccount currentUser) {
    MainMessagesFragment fragment = new MainMessagesFragment();
    Bundle args = new Bundle();
    args.putSerializable("account", currentUser);
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (getArguments() != null) {
      currentUser = (RedditAccount) getArguments().getSerializable("account");
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    Log.d("pre fragment", "before fragment View Set");
    View v = inflater.inflate(R.layout.activity_messages_fragment, container,false);
    Log.d("post fragment", "after fragment View Set");

    TextView userTv = (TextView) v.findViewById(R.id.username_tv);

    userTv.setText(String.format(getString(R.string.login_complete_welcome_user), currentUser.getUsername()));

    messageRv = (RecyclerView) v.findViewById(R.id.message_rv);
    messageRv.setLayoutManager(new LinearLayoutManager(getContext()));
    messageRv.setAdapter(messageConversationAdapter);
    ((LinearLayoutManager) messageRv.getLayoutManager()).setReverseLayout(true);
    ((LinearLayoutManager) messageRv.getLayoutManager()).setStackFromEnd(true);

    swipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.activities_messages_swiperefresh);
    Snackbar snack = Snackbar.make(getActivity().findViewById(R.id.messages_activity_fragholder), "Loading messages...", Snackbar.LENGTH_INDEFINITE);

    //Check if user already exists.
    Log.d(currentUser.getUsername(), " account is " + ((currentUser.getAccountIsNew()) ? "new" : "not new"));
    if (currentUser.getAccountIsNew()) {
      snack.show();
      loadAllPastMessages();
    } else {
      Singleton.get().getDb().messages().getNewestMessageForAllConversationsForUser(currentUser.getUsername())
          .observeOn(Schedulers.io())
          .subscribeOn(AndroidSchedulers.mainThread())
          .subscribe(loadedMessages -> {
            List<Conversation> conversations = new ArrayList<>();
            for (Message message : loadedMessages) {
              conversations.add(new Conversation(message.getParentMessageName(),message));
            }
            messageConversationAdapter.addConversations(conversations);
          });
    }

    //TODO: Expand and view conversation
    //TODO: Replying to messages
    //TODO: Message notifications
    //TODO: Viewing images and webpages in here.

    swipeRefreshLayout.setEnabled(true);
    swipeRefreshLayout.setOnRefreshListener(() -> {
      messageConversationAdapter.animateRemoval();
      updateMessagesAndView();
    });

    return v;
  }

  void loadAllPastMessages() {
    swipeRefreshLayout.setRefreshing(true);
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
                    messageConversationAdapter.clearAndReplaceConversations(conversations);

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

  void updateMessagesAndView() {
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
                          messageConversationAdapter.clearAndReplaceConversations(conversations);
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
