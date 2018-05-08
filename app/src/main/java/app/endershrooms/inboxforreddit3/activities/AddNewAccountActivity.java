package app.endershrooms.inboxforreddit3.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import app.endershrooms.inboxforreddit3.R;
import app.endershrooms.inboxforreddit3.RxBus;
import app.endershrooms.inboxforreddit3.RxBus.Subjects;
import app.endershrooms.inboxforreddit3.Singleton;
import app.endershrooms.inboxforreddit3.account.Authentication;
import app.endershrooms.inboxforreddit3.fragments.LoginFragment;
import app.endershrooms.inboxforreddit3.fragments.LoginFragment.OnLoginCompleted;
import app.endershrooms.inboxforreddit3.models.RedditAccount;
import io.reactivex.schedulers.Schedulers;

public class AddNewAccountActivity extends AppCompatActivity implements OnLoginCompleted {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_add_new_account);
    getSupportActionBar().hide();
    getSupportFragmentManager()
        .beginTransaction()
        .add(R.id.fragment_holder, LoginFragment.newInstance())
        .commit();
  }

  @Override
  public void startLoginProgress(String code) {
    Singleton.get().getRedditApi().getAccessTokenFromCode(Authentication.authorizationHeader, new Authentication.Params.NewTokenParams(code))
        .subscribe(jsonLoginResponse -> {
          Singleton.get().getRedditApi().getMe(RedditAccount.getAuthentication(jsonLoginResponse.access_token))
              .subscribeOn(Schedulers.io())
              .subscribe(jsonMeResponse -> {
                RedditAccount newAccount = new RedditAccount(jsonMeResponse.name, jsonLoginResponse);
                Singleton.get().getDb().accounts().addAccount(newAccount);
                RxBus.publish(Subjects.ON_ACCOUNT_ADDED, newAccount.getUsername());
                finish();
              });
        });
  }
}
