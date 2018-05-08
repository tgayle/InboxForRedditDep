package app.endershrooms.inboxforreddit3.activities;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import app.endershrooms.inboxforreddit3.R;
import app.endershrooms.inboxforreddit3.adapters.AccountsListAdapter;
import app.endershrooms.inboxforreddit3.viewmodels.MessagesActivityViewModel;
import com.jakewharton.rxbinding2.view.RxView;
/**
 * Created by Travis on 1/20/2018.
 */

public class MessagesActivity extends BaseActivity {
  AccountsListAdapter accountsListAdapter;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_messages_with_drawer);
    MessagesActivityViewModel model = ViewModelProviders.of(this).get(MessagesActivityViewModel.class);
    accountsListAdapter = new AccountsListAdapter();

    getSupportActionBar().hide();
    model.setCurrentAccount((String) getIntent().getSerializableExtra("account"));

    ImageButton drawerExpandAccountsBtn = (ImageButton) findViewById(R.id.drawer_expandusers_btn);
    final RecyclerView drawerAccountSwitcher = (RecyclerView) findViewById(R.id.activity_messages_drawer_users_list);
    drawerAccountSwitcher.setLayoutManager(new LinearLayoutManager(MessagesActivity.this));
    drawerAccountSwitcher.setAdapter(accountsListAdapter);
    TextView drawerUsernameTv = findViewById(R.id.main_drawer_navheader_username);

    model.getAccountsAsPagedList().observe(this, list -> {
      accountsListAdapter.submitList(list);
    });

    model.getCurrentAccount().observe(this, currentAccount -> {
      if (currentAccount != null) {
        drawerUsernameTv.setText(currentAccount.getUsername());
      }
    });

//TODO:
//    getSupportFragmentManager()
//        .beginTransaction()
//        .add(R.id.messages_activity_fragholder,MainMessagesFragment.newInstance(currentUser))
//        .commit();



    RxView.clicks(drawerExpandAccountsBtn)
        .subscribe(aVoid -> {
          if (drawerAccountSwitcher.getVisibility() == View.GONE) {
            drawerAccountSwitcher.setVisibility(View.VISIBLE);
            drawerExpandAccountsBtn.setRotation(180f);
          } else {
            drawerAccountSwitcher.setVisibility(View.GONE);
            drawerExpandAccountsBtn.setRotation(0f);
          }
        });


  }


//  void setAccountChangeListener() {
//    //Only go into a new activity if the current user isn't new.
//    RxBus.subscribe(Subjects.ON_ACCOUNT_ADDED, this, obj -> {
//      String newAccountName = (String) obj;
//      Singleton.get().getDb().accounts().getAccountFromName(newAccountName)
//          .subscribeOn(Schedulers.io())
//          .subscribe(newAccount -> {
//            if (newAccount.getAccountIsNew() && !currentUser.getAccountIsNew()) {
//              newAccount.setAccountIsNew(false);
//              currentUser = newAccount;
//              Single.fromCallable(() -> Singleton.get().getDb().accounts().updateAccount(newAccount))
//                  .subscribeOn(Schedulers.io())
//                  .subscribe((integer, throwable) -> {
//                    getSupportFragmentManager()
//                        .beginTransaction()
//                        .replace(R.id.messages_activity_fragholder, MainMessagesFragment.newInstance(newAccount))
//                        .commitAllowingStateLoss();
//                  });
//            }
//          });
//    });

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


  //}

  @Override
  protected void onResume() {
    super.onResume();
//    setAccountChangeListener();
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