package app.endershrooms.inboxforreddit3.activities;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import app.endershrooms.inboxforreddit3.R;
import app.endershrooms.inboxforreddit3.adapters.WelcomeActivityViewPagerAdapter;
import app.endershrooms.inboxforreddit3.models.reddit.RedditAccount;
import app.endershrooms.inboxforreddit3.viewmodels.EntryLoginActivityViewModel;
import app.endershrooms.inboxforreddit3.views.NoSwipeViewPager;

public class EntryLoginActivity extends AppCompatActivity {

  NoSwipeViewPager viewPager;
  WelcomeActivityViewPagerAdapter vpAdapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    getSupportActionBar().hide();
    viewPager = (NoSwipeViewPager) findViewById(R.id.vpager);
    viewPager.setPagingEnabled(false);
    vpAdapter = new WelcomeActivityViewPagerAdapter(getSupportFragmentManager());
    viewPager.setAdapter(vpAdapter);
    viewPager.setCurrentItem(0);

    EntryLoginActivityViewModel viewModel = ViewModelProviders.of(this).get(EntryLoginActivityViewModel.class);

    viewModel.getAllAccounts().observe(this, redditAccounts -> {
      if (redditAccounts != null) {
        for (RedditAccount redditAccount : redditAccounts) {
          viewModel.onUserDetected(redditAccount);
        }
      }
    });

    viewModel.getAddedAccount().observe(this, account -> {
      if (account != null) {
        finish(); //End activity when account found.
      }
    });

    viewModel.getCurrentLoginProgress().observe(this, progress -> {
      if (progress != null) {
        switch (progress) {
          case WELCOME:
            break;
          case LOGIN:
            viewPager.setCurrentItem(1);
            break;
          case LOADING:
            viewPager.setCurrentItem(2);
            break;
        }
      }
    });

  }

}
