package app.endershrooms.inboxforreddit3.viewmodels.login;

import android.annotation.SuppressLint;
import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;
import app.endershrooms.inboxforreddit3.Constants;
import app.endershrooms.inboxforreddit3.Singleton;
import app.endershrooms.inboxforreddit3.account.Authentication;
import app.endershrooms.inboxforreddit3.activities.MessagesActivity;
import app.endershrooms.inboxforreddit3.fragments.WelcomeActivityFragment.FragmentLoadingProgress;
import app.endershrooms.inboxforreddit3.models.reddit.RedditAccount;
import app.endershrooms.inboxforreddit3.models.reddit.ResponseWithError;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import java.util.UUID;

/**
 * Created by Travis on 5/8/2018.
 */

public abstract class BaseLoginViewModel extends AndroidViewModel {

  protected MutableLiveData<RedditAccount> addedAccount = new MutableLiveData<>();
  protected SharedPreferences sharedPreferences;
  protected String loginAuthState = UUID.randomUUID().toString();
  protected MutableLiveData<String> loginRedirectCode = new MutableLiveData<>();
  protected MutableLiveData<FragmentLoadingProgress> currentProgress = new MutableLiveData<>();
  protected MutableLiveData<String> loginProgressText = new MutableLiveData<>();

  public BaseLoginViewModel(@NonNull Application application) {
    super(application);
  }

  public LiveData<RedditAccount> getAddedAccount() {
    return addedAccount;
  }

  public void setAddedAccount(RedditAccount acc) {
    addedAccount.postValue(acc);
    sharedPreferences.edit().putString(Constants.SHARED_PREFS_CURRENT_ACC, acc.getUsername()).apply(); //set current user
    Intent i = new Intent(getApplication().getApplicationContext(), MessagesActivity.class);
    getApplication().getApplicationContext().startActivity(i);
    Log.v("BaseLoginViewModel",
        "Going into Messages with " + acc.getUsername());
  }

  public String getLoginAuthState() {
    return loginAuthState;
  }

  public void addAccountToDatabase(RedditAccount account) {
    Single.fromCallable(() -> Singleton.get().getDb().accounts().addAccount(account))
        .subscribeOn(Schedulers.io())
        .subscribe();
  }

  public LiveData<FragmentLoadingProgress> getCurrentLoginProgress() {
    return currentProgress;
  }

  public void changeLoginProgress(FragmentLoadingProgress progress) {
    currentProgress.setValue(progress);
  }

  public LiveData<String> getLoginProgressText() {
    return loginProgressText;
  }

  public void setLoginProgressText(String update) {
    loginProgressText.postValue(update);
  }

  public ResponseWithError<Boolean, LoginWebviewResult> processLoadedPage(String url) {
    ResponseWithError<Boolean, LoginWebviewResult> response = new ResponseWithError<>();

    if (url.contains("&gws_rd=ssl") || url.contains("?state=" + loginAuthState)) {
      response.setData(true);
      final String redirect_uri = url.replace("https://www.google.com/?", ""); //webview.getUrl vs using normal url?
      String[] params = redirect_uri.split("&");

      String redirect_state = params[0].replace("state=", "");
      String redirect_code = params[1].replace("code=", "");

      if (!loginAuthState.equalsIgnoreCase(redirect_state)) {
        response.setError(LoginWebviewResult.INCORRECT_STATE);
      } else {
        Log.v("OauthLogin", "States did match! Continuing!");
      }

      if (url.contains("access_denied")) {
        response.setError(LoginWebviewResult.ACCESS_DENIED);
      }

      loginRedirectCode.setValue(redirect_code);
    } else {
      response.setData(false);
      response.setError(LoginWebviewResult.UNKNOWN);
    }
    if (response.getData() && response.getError() == null) {
      changeLoginProgress(FragmentLoadingProgress.LOADING);
      handleLogin(loginRedirectCode.getValue());
    }
    return response;
  }

  @SuppressLint("CheckResult")
  private void handleLogin(String code) {
    Singleton.get().getRedditApi().getAccessTokenFromCode(Authentication.basicAuthorizationHeader, new Authentication.Params.NewTokenParams(code))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(jsonLoginResponse -> {
          Singleton.get().getRedditApi().getMe(RedditAccount.getAuthentication(jsonLoginResponse.access_token))
              .subscribeOn(Schedulers.io())
              .observeOn(AndroidSchedulers.mainThread())
              .subscribe(jsonMeResponse -> {
                RedditAccount newAccount= new RedditAccount(jsonMeResponse.name, jsonLoginResponse);
                addAccountToDatabase(newAccount);
                setAddedAccount(newAccount);
              });
        });
  }

  public enum LoginWebviewResult {
    ACCESS_DENIED,
    INCORRECT_STATE,
    UNKNOWN
  }
}
