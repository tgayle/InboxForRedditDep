package app.endershrooms.inboxforreddit3.activities;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.SimpleDrawerListener;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import app.endershrooms.inboxforreddit3.R;
import app.endershrooms.inboxforreddit3.adapters.AccountsListAdapter;
import app.endershrooms.inboxforreddit3.conductor.MainMessagesController;
import app.endershrooms.inboxforreddit3.interfaces.OnAccountListInteraction;
import app.endershrooms.inboxforreddit3.models.reddit.RedditAccount;
import app.endershrooms.inboxforreddit3.viewmodels.MessagesActivityViewModel;
import com.bluelinelabs.conductor.Conductor;
import com.bluelinelabs.conductor.Router;
import com.bluelinelabs.conductor.RouterTransaction;

/**
 * Created by Travis on 1/20/2018.
 */

public class MessagesActivity extends BaseActivity implements OnAccountListInteraction {

  AccountsListAdapter accountsListAdapter;
  MessagesActivityViewModel model;

  DrawerLayout drawer;
  private Router router;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_messages_with_drawer);
    model = ViewModelProviders.of(this).get(MessagesActivityViewModel.class);

    router = Conductor.attachRouter(this, findViewById(R.id.messages_activity_fragholder), savedInstanceState);
    if (!router.hasRootController()) {
      router.setRoot(RouterTransaction.with(new MainMessagesController()));
    }

    ImageButton drawerExpandAccountsBtn = (ImageButton) findViewById(R.id.drawer_expandusers_btn);
    View drawerHeader = findViewById(R.id.main_drawer_navheader);
    final RecyclerView drawerAccountSwitcher = (RecyclerView) findViewById(R.id.activity_messages_drawer_users_list);
    drawerAccountSwitcher.setLayoutManager(new LinearLayoutManager(MessagesActivity.this));
    TextView drawerUsernameTv = findViewById(R.id.main_drawer_navheader_username);

    drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
    accountsListAdapter = new AccountsListAdapter(this);
    drawerAccountSwitcher.setAdapter(accountsListAdapter);
    model.getDataModel().getAccountsAsPagedList().observe(this, list -> accountsListAdapter.submitList(list));

    model.getDataModel().getCurrentAccount().observe(this, redditAccount ->{
      if (redditAccount != null) {
        if (model.shouldReturnToLoginScreen(redditAccount)) {
          finish();
          Intent goBackToLogin = new Intent(this, EntryLoginActivity.class);
          startActivity(goBackToLogin);
        }
        drawerUsernameTv.setText(redditAccount.getUsername());
      }
    });

    drawerHeader.setOnClickListener(view -> {
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
  public void onAccountSelected(RedditAccount account) {
    drawer.addDrawerListener(new SimpleDrawerListener() {
      @Override
      public void onDrawerClosed(View drawerView) {
        super.onDrawerClosed(drawerView);
        drawer.removeDrawerListener(this);
        model.initAccountSwitch(account);
      }
    });
    drawer.closeDrawer(GravityCompat.START);
  }

  @Override
  public void onAccountRemoved(RedditAccount account) {
    model.removeAccount(account);
  }


  @Override
  protected void onResume() {
    super.onResume();
    drawer.closeDrawer(GravityCompat.START);
  }

  @Override
  public void onBackPressed() {
    if (drawer.isDrawerOpen(GravityCompat.START)) {
      drawer.closeDrawer(GravityCompat.START);
    } else {
      if (!router.handleBack()) {
        super.onBackPressed();
      }
    }
  }

}