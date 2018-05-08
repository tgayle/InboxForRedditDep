package app.endershrooms.inboxforreddit3.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import app.endershrooms.inboxforreddit3.R;
import app.endershrooms.inboxforreddit3.Singleton;
import app.endershrooms.inboxforreddit3.account.Authentication;
import app.endershrooms.inboxforreddit3.adapters.WelcomeActivityViewPagerAdapter;
import app.endershrooms.inboxforreddit3.database.DatabaseListener;
import app.endershrooms.inboxforreddit3.fragments.LoginFragment.OnLoginCompleted;
import app.endershrooms.inboxforreddit3.interfaces.StartLogin;
import app.endershrooms.inboxforreddit3.models.RedditAccount;
import app.endershrooms.inboxforreddit3.views.NoSwipeViewPager;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import java.util.ArrayList;
import java.util.List;

public class EntryLoginActivity extends AppCompatActivity implements OnLoginCompleted, StartLogin {

  NoSwipeViewPager viewPager;
  WelcomeActivityViewPagerAdapter vpAdapter;
  private List<LoginUpdateListener> mListeners = new ArrayList<>();
  LoginUpdateListener fragmentLoginListener;
  Disposable checkNumUsers;

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

    Singleton.get().prepareDatabase(EntryLoginActivity.this);
    DatabaseListener.getInstance().prepareListeners();

    checkNumUsers = Singleton.get().getDb().accounts().getAllAccounts()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(accounts -> {
          if (accounts.size() > 0) {
            Intent i = new Intent(EntryLoginActivity.this, MessagesActivity.class);
            i.putExtra("account", accounts.get(0).getUsername()); //TODO: get current user from shared prefs
            Log.v("EntryLoginActivity",
                "Going into Messages with " + accounts.get(0).getUsername() + " from db.");
            startActivity(i);
            this.finish();
          }
        });

  }

  @Override
  public void startLoginProgress(String code) {
    viewPager.setCurrentItem(2);
    Singleton.get().getRedditApi().getAccessTokenFromCode(Authentication.authorizationHeader, new Authentication.Params.NewTokenParams(code))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(jsonLoginResponse -> {
          Singleton.get().getRedditApi().getMe(RedditAccount.getAuthentication(jsonLoginResponse.access_token))
              .subscribeOn(Schedulers.io())
              .observeOn(AndroidSchedulers.mainThread())
              .subscribe(jsonMeResponse -> {
                fragmentLoginListener.onCompleteLogin(new RedditAccount(jsonMeResponse.name, jsonLoginResponse));
              });
        });
  }

  public synchronized void registerDataUpdateListener(LoginUpdateListener listener) {
    mListeners.add(listener);
  }

  public synchronized void unregisterDataUpdateListener(LoginUpdateListener listener) {
    mListeners.remove(listener);
  }

  public synchronized void updateLoadingTextProgress(final String text) {
    for (final LoginUpdateListener listener : mListeners) {
      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          listener.updateLoadingText(text);
        }
      });

    }
  }

  public void setFragmentLoginListener(
      LoginUpdateListener fragmentLoginListener) {
    this.fragmentLoginListener = fragmentLoginListener;
  }

  @Override
  public void startLogin() {
    viewPager.setCurrentItem(1);
  }

  public interface LoginUpdateListener {
    void updateLoadingText(String text);
    void onCompleteLogin(RedditAccount account);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    checkNumUsers.dispose(); //Make sure to dispose when done here.
    System.out.println("Checking users is disposed.");
  }
}
