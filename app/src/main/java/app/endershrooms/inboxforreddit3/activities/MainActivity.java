package app.endershrooms.inboxforreddit3.activities;

import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import app.endershrooms.inboxforreddit3.R;
import app.endershrooms.inboxforreddit3.Singleton;
import app.endershrooms.inboxforreddit3.adapters.WelcomeActivityViewPagerAdapter;
import app.endershrooms.inboxforreddit3.fragments.LoginFragment.OnLoginCompleted;
import app.endershrooms.inboxforreddit3.interfaces.StartLogin;
import app.endershrooms.inboxforreddit3.models.RedditAccount;
import app.endershrooms.inboxforreddit3.net.Authentication;
import app.endershrooms.inboxforreddit3.net.JSONLoginResponse;
import app.endershrooms.inboxforreddit3.views.NoSwipeViewPager;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnLoginCompleted, StartLogin {

  NoSwipeViewPager viewPager;
  WelcomeActivityViewPagerAdapter vpAdapter;
  private List<LoginUpdateListener> mListeners = new ArrayList<>();
  LoginUpdateListener fragmentLoginListener;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    getSupportActionBar().hide();
    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
    viewPager = (NoSwipeViewPager) findViewById(R.id.vpager);
    viewPager.setPagingEnabled(false);
    vpAdapter = new WelcomeActivityViewPagerAdapter(getSupportFragmentManager());

    viewPager.setAdapter(vpAdapter);
    viewPager.setCurrentItem(0);

    Singleton.get().prepareDatabase(MainActivity.this);

//    Singleton.get().getDb().accounts().getAllAccounts().subscribeOn(Schedulers.io())
//        .observeOn(AndroidSchedulers.mainThread())
//        .subscribe(accounts -> {
//          if (accounts.size() > 0) {
//            Intent i = new Intent(MainActivity.this, MessagesActivity.class);
//            i.putExtra("account", accounts.get(0));
//            startActivity(i);
//            finish();
//          }
//        });

  }


  @Override
  public void startLoginProgress(String code) {
    viewPager.setCurrentItem(2);
//    Login login = new Login(code, MainActivity.this, new Observer<String>() {
//      @Override
//      public void onSubscribe(Disposable d) {
//
//      }
//
//      @Override
//      public void onNext(String s) {
//        fragmentLoginListener.updateLoadingText(s);
//      }
//
//      @Override
//      public void onError(Throwable e) {
//
//      }
//
//      @Override
//      public void onComplete() {
//
//      }
//    }, new SingleObserver<RedditAccount>() {
//      @Override
//      public void onSubscribe(Disposable d) {
//
//      }
//
//      @Override
//      public void onSuccess(RedditAccount account) {
//        fragmentLoginListener.onCompleteLogin(account);
//      }
//
//      @Override
//      public void onError(Throwable e) {
//
//      }
//    });

    Singleton.get().getRedditApiNonOauth().getAccessTokenFromCode(Authentication.authorizationHeader, new Authentication.Params(code))
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(jsonLoginResponse -> {
          Singleton.get().getRedditApiOauth().getMe(RedditAccount.getAuthentication(jsonLoginResponse.access_token))
              .subscribeOn(Schedulers.io())
              .observeOn(AndroidSchedulers.mainThread())
              .subscribe(jsonMeResponse -> {
                fragmentLoginListener.onCompleteLogin(new RedditAccount(jsonMeResponse.name, jsonLoginResponse.access_token, jsonLoginResponse.refresh_token,
                    JSONLoginResponse.expiresInDate(jsonLoginResponse.expires_in)));
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


}
