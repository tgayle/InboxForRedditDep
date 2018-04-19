package app.endershrooms.inboxforreddit3.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import app.endershrooms.inboxforreddit3.APIManager;
import app.endershrooms.inboxforreddit3.R;
import app.endershrooms.inboxforreddit3.RecyclerViewVerticalSpace;
import app.endershrooms.inboxforreddit3.Singleton;
import app.endershrooms.inboxforreddit3.adapters.AccountsListAdapter;
import app.endershrooms.inboxforreddit3.adapters.MessagesConversationRecyclerViewAdapter;
import app.endershrooms.inboxforreddit3.adapters.MessagesRecyclerViewAdapter;
import app.endershrooms.inboxforreddit3.models.Conversation;
import app.endershrooms.inboxforreddit3.models.Message;
import app.endershrooms.inboxforreddit3.models.RedditAccount;
import app.endershrooms.inboxforreddit3.net.model.Data;
import app.endershrooms.inboxforreddit3.net.model.IndividualMessageResponseModel;
import com.jakewharton.rxbinding2.view.RxView;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import java.util.ArrayList;
import java.util.List;
/**
 * Created by Travis on 1/20/2018.
 */

public class MessagesActivity extends AppCompatActivity {
  RedditAccount currentUser;
  MessagesConversationRecyclerViewAdapter messageConversationAdapter = new MessagesConversationRecyclerViewAdapter(new ArrayList<>());
  AccountsListAdapter accountsListAdapter = new AccountsListAdapter(new ArrayList<>());
  SwipeRefreshLayout swipeRefreshLayout;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_messages_with_drawer);
    getSupportActionBar().hide();
    System.out.println("MessagesActivity started");
    currentUser = (RedditAccount) getIntent().getSerializableExtra("account");

    TextView userTv = (TextView) findViewById(R.id.username_tv);
    //Dates appearing wrong and conversions look odd. TODO:
    //TODO: Implement swipe refresh.

    userTv.setText(String.format(getString(R.string.login_complete_welcome_user), currentUser.getUsername()));

    final RecyclerView messageRv = (RecyclerView) findViewById(R.id.message_rv);
    messageRv.setLayoutManager(new LinearLayoutManager(MessagesActivity.this));
    messageRv.setAdapter(messageConversationAdapter);
    ((LinearLayoutManager) messageRv.getLayoutManager()).setReverseLayout(true);
    ((LinearLayoutManager) messageRv.getLayoutManager()).setStackFromEnd(true);

    final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
    ImageButton drawerExpandAccountsBtn = (ImageButton) findViewById(R.id.drawer_expandusers_btn);

    final RecyclerView drawerAccountSwitcher = (RecyclerView) findViewById(R.id.activity_messages_drawer_users_list);
    drawerAccountSwitcher.setLayoutManager(new LinearLayoutManager(MessagesActivity.this));
    drawerAccountSwitcher.setAdapter(accountsListAdapter);
    drawerAccountSwitcher.addItemDecoration(new RecyclerViewVerticalSpace(16));

    Singleton.get().getDb().accounts().getAllAccounts()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(accounts -> {
          for (RedditAccount account : accounts) {
            Log.v("MessagesActivity", account.loggingInfo());
          }
          accountsListAdapter.addAccounts(accounts);
        });


    swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.activities_messages_swiperefresh);
    swipeRefreshLayout.setOnRefreshListener(() -> {
      messageConversationAdapter.animateRemoval();
//      updateMessagesAndView(() -> {
//        swipeRefreshLayout.setRefreshing(false);
//      });
    });

    APIManager.get().updateUserToken(currentUser, () -> {
      System.out.println("update token begin from main activity.");
      APIManager.get().downloadAllPastMessages(currentUser, "inbox", 20, "", (after, messagesLoaded) -> {
        if (after == null || after.equals("null")) {
          System.out.println("Download ended from Activity: after = " + after + " and messagesLoaded was " + messagesLoaded);
          Singleton.get().getDb().messages().getAllUserMessagesAsc(currentUser.getUsername())
              .subscribeOn(Schedulers.io())
              .observeOn(AndroidSchedulers.mainThread())
              .subscribe(messages -> {
                System.out.println("From Activity: messages size is " + messages.size());
                messageRv.setAdapter(new MessagesRecyclerViewAdapter(messages));
              });
        }

      });
    });

    RxView.clicks(drawerExpandAccountsBtn)
        .subscribe(voida -> {
          if (drawerAccountSwitcher.getVisibility() == View.GONE) {
            drawerAccountSwitcher.setVisibility(View.VISIBLE);
            drawerExpandAccountsBtn.setRotation(180f);
          } else {
            drawerAccountSwitcher.setVisibility(View.GONE);
            drawerExpandAccountsBtn.setRotation(0f);

          }
        });

    RxView.clicks(userTv)
        .subscribe(avoid -> {
          Singleton.get().getDb().accounts().getAllAccounts()
              .subscribeOn(Schedulers.io())
              .observeOn(AndroidSchedulers.mainThread())
              .subscribe(accounts -> {
                for (RedditAccount acc : accounts) {
                  Log.v("LoadAccounts", String.format("%s %s %s", acc.getUsername(), acc.getAccessToken(), acc.getRefreshToken()));
                }
              });
        });

  }

//  void updateMessagesAndView(OnCompleteInterface onCompleteInterface) {
//    swipeRefreshLayout.setRefreshing(true);
//    APIManager.get().getAllPastMessages(currentUser, () -> {
//      Single.fromCallable(() -> Singleton.get().getDb().messages().getAllUserMessagesAsc(currentUser.getUsername()))
//          .subscribeOn(Schedulers.io())
//          .observeOn(Schedulers.computation())
//          .subscribe(messages -> {
//            Single.fromCallable(() ->Conversation.separateMessagesIntoConversations(messages))
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(conversations -> {
//                  messageConversationAdapter.addConversations(conversations);
//                  onCompleteInterface.onComplete();
//                });
//          });
//    });
//  }

  List<Message> getMessages(RedditAccount acc, String where) {
    List<Message> messages = new ArrayList<>();
    Singleton.get().getRedditApiOauth().getMessages(acc.getAuthentication(), where, 30, "")
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(messagesJSONResponse -> {
          //look to putting on computation thread
          for (IndividualMessageResponseModel child : messagesJSONResponse.data.children) {
            String name = child.data.name;
            String parent_name = child.data.first_message_name;
            if (parent_name == null || parent_name.equals("null")) parent_name = name;

            Data messageData = child.data;

            String messageContent = child.data.body_html.replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&apos;", "'")
                .replace("&amp;", "&")
                .replace("<li><p>", "<p>• ")
                .replace("</li>", "<br>")
                .replaceAll("<li.*?>", "•")
                .replace("<p>", "<div>")
                .replace("</p>", "</div>");

            messageContent = messageContent.substring(0, messageContent.lastIndexOf("\n"));

            Message thisMsg = new Message(currentUser.getUsername(), name, parent_name, messageData.author, messageData.dest, messageData.subject, messageContent, messageData.created_utc, messageData.isNew);
            messages.add(thisMsg);
          }

          Completable.fromAction(() -> Singleton.get().getDb().messages().insertMessages(messages))
              .subscribeOn(Schedulers.io())
              .observeOn(AndroidSchedulers.mainThread())
              .subscribe();

          Single.fromCallable(() -> Conversation.separateMessagesIntoConversations(messages))
              .subscribeOn(Schedulers.computation())
              .observeOn(AndroidSchedulers.mainThread())
              .subscribe(conversations -> {
                messageConversationAdapter.addConversations(conversations);
              });
        });
    return messages;
  }



  @Override
  public void onBackPressed() {
    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
    if (drawer.isDrawerOpen(GravityCompat.START)) {
      drawer.closeDrawer(GravityCompat.START);
    } else {
      super.onBackPressed();
    }
  }
}