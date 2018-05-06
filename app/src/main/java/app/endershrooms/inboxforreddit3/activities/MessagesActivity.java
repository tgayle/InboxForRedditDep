package app.endershrooms.inboxforreddit3.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import app.endershrooms.inboxforreddit3.R;
import app.endershrooms.inboxforreddit3.RxBus;
import app.endershrooms.inboxforreddit3.RxBus.Subjects;
import app.endershrooms.inboxforreddit3.Singleton;
import app.endershrooms.inboxforreddit3.adapters.AccountsListAdapter;
import app.endershrooms.inboxforreddit3.adapters.MessagesConversationRecyclerViewAdapter;
import app.endershrooms.inboxforreddit3.models.Conversation;
import app.endershrooms.inboxforreddit3.models.RedditAccount;
import app.endershrooms.inboxforreddit3.net.APIManager;
import com.jakewharton.rxbinding2.view.RxView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import java.util.ArrayList;
/**
 * Created by Travis on 1/20/2018.
 */

public class MessagesActivity extends BaseActivity {
  RedditAccount currentUser;
  MessagesConversationRecyclerViewAdapter messageConversationAdapter = new MessagesConversationRecyclerViewAdapter(new ArrayList<>());
  AccountsListAdapter accountsListAdapter = new AccountsListAdapter(new ArrayList<>());
  SwipeRefreshLayout swipeRefreshLayout;
  RecyclerView messageRv;


  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_messages_with_drawer);
    getSupportActionBar().hide();
    System.out.println("MessagesActivity started");
    currentUser = (RedditAccount) getIntent().getSerializableExtra("account");

    TextView userTv = (TextView) findViewById(R.id.username_tv);

    userTv.setText(String.format(getString(R.string.login_complete_welcome_user), currentUser.getUsername()));

    messageRv = (RecyclerView) findViewById(R.id.message_rv);
    messageRv.setLayoutManager(new LinearLayoutManager(MessagesActivity.this));
    messageRv.setAdapter(messageConversationAdapter);
    ((LinearLayoutManager) messageRv.getLayoutManager()).setReverseLayout(true);
    ((LinearLayoutManager) messageRv.getLayoutManager()).setStackFromEnd(true);

    ImageButton drawerExpandAccountsBtn = (ImageButton) findViewById(R.id.drawer_expandusers_btn);
    swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.activities_messages_swiperefresh);

    final RecyclerView drawerAccountSwitcher = (RecyclerView) findViewById(R.id.activity_messages_drawer_users_list);
    drawerAccountSwitcher.setLayoutManager(new LinearLayoutManager(MessagesActivity.this));
    drawerAccountSwitcher.setAdapter(accountsListAdapter);

    TextView drawerUsernameTv = findViewById(R.id.main_drawer_navheader_username);
    drawerUsernameTv.setText(currentUser.getUsername());

    Singleton.get().getDb().accounts().getAllAccounts()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(accounts -> {
          for (RedditAccount account : accounts) {
            Log.v("MessagesActivity", account.loggingInfo());
          }
          accountsListAdapter.updateAccounts(accounts);
        });

    //Check if user already exists.c
    if (currentUser.getAccountIsNew()) {
      Snackbar.make(messageRv, "Loading messages...", Snackbar.LENGTH_SHORT).show();
      loadAllPastMessages();
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

    Singleton.get().getDb().messages().getAllUserMessagesAsc(currentUser.getUsername())
        .subscribeOn(Schedulers.io())
        .observeOn(Schedulers.computation())
        .map(Conversation::formConversations)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(conversations -> {
          messageConversationAdapter.animateRemoval();
          messageConversationAdapter.clearAndReplaceConversations(conversations);
          updateMessagesAndView();
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

  }

//  void isAccountNew(RedditAccount account, DoesAccountExistInterface doesAccountExistInterface) {
//    Singleton.get().getDb().messages().getFirstMessage(account.getUsername())
//        .subscribeOn(Schedulers.io())
//        .observeOn(AndroidSchedulers.mainThread())
//        .subscribe((messageList, throwable) -> {
//          Log.d("GetFirstMessage", "list is " + messageList.size());
//          if (messageList.size() > 0) {
//            doesAccountExistInterface.accountExists(true);
//            //this account already existed in the db
//          } else {
//            //TODO: Offer to download all past messages.
//            doesAccountExistInterface.accountExists(false);
//          }
//          if (throwable != null) {
//            Log.d("GetFirstMessage", "throwable not null and list is " + messageList);
//          }
//        });
//  }

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
                  });
            }
          }), throwable -> {

          });
        }
      }, throwable -> {
          Snackbar.make(findViewById(R.id.drawer_layout), "Issue loading messages " + "loadAllPastMessages()", Snackbar.LENGTH_SHORT).show();
          swipeRefreshLayout.setRefreshing(false);
      });
    }, tokenError -> {
      Snackbar.make(findViewById(R.id.drawer_layout), "Error updating token", Snackbar.LENGTH_LONG).setAction("Log Error", (view) -> {
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
                  Snackbar.make(findViewById(R.id.drawer_layout), "There was an issue loading messages. " + err , Snackbar.LENGTH_SHORT).show();
                });
          }, (err) -> {
            System.out.println("There was an issue loading the newest message : " + err);
          });

    }, tokenError -> {
      Snackbar.make(messageRv, "Error updating token", Snackbar.LENGTH_LONG).setAction("Log Error", (view) -> {
        System.out.println(tokenError);
      }).show();
    });
  }

  void setAccountChangeListener() {
    //Only go into a new activity if the current user isn't new.

    RxBus.subscribe(Subjects.ON_ACCOUNT_UPDATE, this, obj -> {
      RedditAccount newAccount = (RedditAccount) obj;
      System.out.println("Subscribed to account " + newAccount.getUsername());
    });

//    onAccountChange = RxBus.get().onAccountAdded
//        .subscribe(newAccount -> {
//          Log.v("onAccountChange", "New account heard " + newAccount.getUsername());
//          Singleton.get().getDb().accounts().getAccountFromName(currentUser.getUsername())
//              .subscribeOn(Schedulers.io())
//              .observeOn(AndroidSchedulers.mainThread())
//              .subscribe(currentAccount -> {
//                if (newAccount.getAccountIsNew() && !currentAccount.getAccountIsNew()) {
//                  finish();
//                  newAccount.setAccountIsNew(false);
//                  Single.fromCallable(() -> Singleton.get().getDb().accounts().updateAccount(newAccount))
//                      .subscribeOn(Schedulers.io())
//                      .subscribe((integer, throwable) -> {
//                        Intent i = new Intent(MessagesActivity.this, MessagesActivity.class);
//                        i.putExtra("account", newAccount);
//                        startActivity(i);
//                      });
//                }
//              });
//        });

  }

  @Override
  protected void onResume() {
    super.onResume();
    setAccountChangeListener();
  }

  @Override
  protected void onPause() {
    super.onPause();
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