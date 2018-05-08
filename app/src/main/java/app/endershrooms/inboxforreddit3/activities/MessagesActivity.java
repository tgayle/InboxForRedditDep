package app.endershrooms.inboxforreddit3.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
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
import app.endershrooms.inboxforreddit3.fragments.MainMessagesFragment;
import app.endershrooms.inboxforreddit3.models.RedditAccount;
import com.jakewharton.rxbinding2.view.RxView;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import java.util.ArrayList;
/**
 * Created by Travis on 1/20/2018.
 */

public class MessagesActivity extends BaseActivity {
  RedditAccount currentUser;
  AccountsListAdapter accountsListAdapter = new AccountsListAdapter(new ArrayList<>());

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.d("pre activity", "Before Activity View Set");
    setContentView(R.layout.activity_messages_with_drawer);
    Log.d("post activity", "after Activity View Set");

    getSupportActionBar().hide();
    System.out.println("MessagesActivity started");
    currentUser = (RedditAccount) getIntent().getSerializableExtra("account");

    getSupportFragmentManager()
        .beginTransaction()
        .add(R.id.messages_activity_fragholder,MainMessagesFragment.newInstance(currentUser))
        .commit();

    ImageButton drawerExpandAccountsBtn = (ImageButton) findViewById(R.id.drawer_expandusers_btn);
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



  void setAccountChangeListener() {
    //Only go into a new activity if the current user isn't new.
    RxBus.subscribe(Subjects.ON_ACCOUNT_ADDED, this, obj -> {
      String newAccountName = (String) obj;
      Singleton.get().getDb().accounts().getAccountFromName(newAccountName)
          .subscribeOn(Schedulers.io())
          .subscribe(newAccount -> {
            if (newAccount.getAccountIsNew() && !currentUser.getAccountIsNew()) {
              newAccount.setAccountIsNew(false);
              currentUser = newAccount;
              Single.fromCallable(() -> Singleton.get().getDb().accounts().updateAccount(newAccount))
                  .subscribeOn(Schedulers.io())
                  .subscribe((integer, throwable) -> {
                    getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.messages_activity_fragholder, MainMessagesFragment.newInstance(newAccount))
                        .commitAllowingStateLoss();
                  });
            }
          });
    });

//    RxBus.subscribe(Subjects.ON_ACCOUNT_UPDATE, this, obj -> {
//      RedditAccount newAccount = (RedditAccount) obj;
//      System.out.println("Subscribed to account " + newAccount.getUsername());
//        if (newAccount.getAccountIsNew() && !currentUser.getAccountIsNew()) {
//          newAccount.setAccountIsNew(false);
//          Single.fromCallable(() -> Singleton.get().getDb().accounts().updateAccount(newAccount))
//              .subscribeOn(Schedulers.io())
//              .subscribe((integer, throwable) -> {
//                getSupportFragmentManager()
//                    .beginTransaction()
//                    .replace(R.id.messages_activity_fragholder, MainMessagesFragment.newInstance(newAccount))
//                    .commitAllowingStateLoss();
//              });
//        }
//    });


  }

  @Override
  protected void onResume() {
    super.onResume();
    setAccountChangeListener();
    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
    drawer.closeDrawer(GravityCompat.START);
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