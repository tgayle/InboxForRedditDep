package app.endershrooms.inboxforreddit3.activities;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import app.endershrooms.inboxforreddit3.Constants;
import app.endershrooms.inboxforreddit3.R;
import app.endershrooms.inboxforreddit3.adapters.AccountsListAdapter;
import app.endershrooms.inboxforreddit3.fragments.MainMessagesFragment;
import app.endershrooms.inboxforreddit3.interfaces.OnAccountListInteraction;
import app.endershrooms.inboxforreddit3.models.reddit.RedditAccount;
import app.endershrooms.inboxforreddit3.viewmodels.MessagesActivityViewModel;
/**
 * Created by Travis on 1/20/2018.
 */

public class MessagesActivity extends BaseActivity {
  AccountsListAdapter accountsListAdapter;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_messages_with_drawer);
    getSupportActionBar().hide();
    Log.v("############", "###########################");
    ImageButton drawerExpandAccountsBtn = (ImageButton) findViewById(R.id.drawer_expandusers_btn);
    final RecyclerView drawerAccountSwitcher = (RecyclerView) findViewById(R.id.activity_messages_drawer_users_list);
    drawerAccountSwitcher.setLayoutManager(new LinearLayoutManager(MessagesActivity.this));
    TextView drawerUsernameTv = findViewById(R.id.main_drawer_navheader_username);
    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

    MessagesActivityViewModel model = ViewModelProviders.of(this).get(MessagesActivityViewModel.class);
    accountsListAdapter = new AccountsListAdapter(new OnAccountListInteraction() {
      @Override
      public void onAccountSelected(RedditAccount account) {
        model.initAccountSwitch(account);
        Log.d("AccountSelected", account.getUsername() + " selected");
        drawer.closeDrawer(GravityCompat.START);
      }

      @Override
      public void onAccountRemoved(RedditAccount account) {
        model.removeAccount(account);
      }
    });
    drawerAccountSwitcher.setAdapter(accountsListAdapter);

    model.getAccountsAsPagedList().observe(this, list -> {
      accountsListAdapter.submitList(list);
    });

    model.getCurrentUserName().observe(this, name -> {
        if (name != null) {
          if (!name.equals(Constants.USER_REMOVED)) {
            model.getAccount(name).observe(this, account -> {
              if (account != null) {
                RedditAccount liveDataCurrentAccount = model.getCurrentAccount().getValue();
                if (liveDataCurrentAccount == null || !liveDataCurrentAccount.equals(account)) {
                  model.setCurrentAccount(account);
                }
              }
            });
          } else {
            finish();
            Intent goBackToLogin = new Intent(this, EntryLoginActivity.class);
            startActivity(goBackToLogin);
          }
      }
    });

    model.getCurrentAccount().observe(this, currentAccount -> {
      Log.d("MessagesActivity", "Current Account is " + ((currentAccount == null) ? currentAccount : currentAccount.getUsername()));
      if (currentAccount != null) {
        drawerUsernameTv.setText(currentAccount.getUsername());
        Fragment messagesFragment = getSupportFragmentManager().findFragmentByTag("messagesFrag");
        FragmentTransaction fm = getSupportFragmentManager().beginTransaction();

        if (messagesFragment == null) { //Temporarily do nothing if fragment already loaded.
          fm.add(R.id.messages_activity_fragholder, MainMessagesFragment.newInstance(), "messagesFrag");
        } //else {
//          fm.replace(R.id.messages_activity_fragholder, MainMessagesFragment.newInstance(), "messagesFrag");
//        }
        fm.commit();
      }
    });

    drawerExpandAccountsBtn.setOnClickListener(view -> {
      if (drawerAccountSwitcher.getVisibility() == View.GONE) {
        drawerAccountSwitcher.setVisibility(View.VISIBLE);
        drawerExpandAccountsBtn.setRotation(180f);
      } else {
        drawerAccountSwitcher.setVisibility(View.GONE);
        drawerExpandAccountsBtn.setRotation(0f);
      }
    });

  }

  @Override
  protected void onResume() {
    super.onResume();
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