package app.endershrooms.inboxforreddit3.activities;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import app.endershrooms.inboxforreddit3.R;
import app.endershrooms.inboxforreddit3.adapters.AccountsListAdapter;
import app.endershrooms.inboxforreddit3.fragments.MainMessagesFragment;
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

    ImageButton drawerExpandAccountsBtn = (ImageButton) findViewById(R.id.drawer_expandusers_btn);
    final RecyclerView drawerAccountSwitcher = (RecyclerView) findViewById(R.id.activity_messages_drawer_users_list);
    drawerAccountSwitcher.setLayoutManager(new LinearLayoutManager(MessagesActivity.this));
    drawerAccountSwitcher.setAdapter(accountsListAdapter);
    TextView drawerUsernameTv = findViewById(R.id.main_drawer_navheader_username);

    MessagesActivityViewModel model = ViewModelProviders.of(this).get(MessagesActivityViewModel.class);
    accountsListAdapter = new AccountsListAdapter();

    model.setCurrentAccount((String) getIntent().getSerializableExtra("account"));
    //TODO: Get current account from somewhere other than passing it back to viewmodel?

    model.getAccountsAsPagedList().observe(this, list -> {
      accountsListAdapter.submitList(list);
    });

    model.getCurrentAccount().observe(this, currentAccount -> {
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