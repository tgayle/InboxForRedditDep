package app.endershrooms.inboxforreddit3.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import app.endershrooms.inboxforreddit3.Constants;
import app.endershrooms.inboxforreddit3.models.reddit.RedditAccount;
import app.endershrooms.inboxforreddit3.models.reddit.ResponseWithError;
import app.endershrooms.inboxforreddit3.viewmodels.model.MessagesActivityDataModel;
import java.util.List;

/**
 * Created by Travis on 5/7/2018.
 */

public class MessagesActivityViewModel extends AndroidViewModel {
  private MessagesActivityDataModel dataModel = new MessagesActivityDataModel();
  private MutableLiveData<String> currentConversationName = new MutableLiveData<>();
  private MutableLiveData<ResponseWithError<LoadingStatusEnum, String>> loadingStatus = new MutableLiveData<>();
  private SharedPreferences sharedPreferences;
  private MutableLiveData<String> currentUserName = dataModel.getCurrentUserNameAsMutable();
  private LiveData<RedditAccount> currentAccount = dataModel.getCurrentAccount();

  public MessagesActivityViewModel(@NonNull Application application) {
    super(application);
    sharedPreferences = application.getApplicationContext().getSharedPreferences(
        Constants.SHARED_PREFERENCES_MAIN, Context.MODE_PRIVATE);

    String sharedPrefUser = sharedPreferences.getString(Constants.SHARED_PREFS_CURRENT_ACC, null);
    setCurrentUsername(sharedPrefUser);
  }

  private void setCurrentUsername(String userName) {
    Log.d("MessagesActivityViewMod", "Username set to " + userName);
    String sharedPrefName = userName.equals(Constants.USER_REMOVED) ? null : userName;
    sharedPreferences.edit().putString(Constants.SHARED_PREFS_CURRENT_ACC, sharedPrefName).apply();
    currentUserName.setValue(userName);
  }

  public void initAccountSwitch(RedditAccount user) {
    setCurrentUsername(user.getUsername());
    setLoadingStatus(LoadingStatusEnum.DONE);
  }

  public LiveData<String> getCurrentConversationName() {
    return currentConversationName;
  }

  public void setCurrentConversationName(String parentName) {
    currentConversationName.setValue(parentName);
  }

  public MessagesActivityDataModel getDataModel() {
    return dataModel;
  }

  public LiveData<ResponseWithError<String, Throwable>> loadNewestMessages() {
    Log.d("MessagesViewModel", "Load newest " + (currentAccount.getValue() == null ? currentAccount : currentAccount.getValue().getUsername()));
    return dataModel.getMessageRepo().loadNewestMessages(currentAccount.getValue());
  }

  public LiveData<ResponseWithError<String, Throwable>> loadAllMessages() {
    setLoadingStatus(LoadingStatusEnum.LOADING, null);
    LiveData<ResponseWithError<String, Throwable>> result = dataModel.getMessageRepo().loadAllMessages(currentAccount.getValue());
    result.observeForever(new Observer<ResponseWithError<String, Throwable>>() {
      @Override
      public void onChanged(@Nullable ResponseWithError<String, Throwable> stringThrowableResponseWithError) {
        if (stringThrowableResponseWithError.getData() == null) {
          setLoadingStatus(LoadingStatusEnum.DONE);
          result.removeObserver(this);
        }
      }
    });
    return result;
  }

  public LiveData<ResponseWithError<LoadingStatusEnum, String>> getLoadingStatus() {
    return loadingStatus;
  }

  public void setLoadingStatus(LoadingStatusEnum status) {
    loadingStatus.postValue(new ResponseWithError<>(status, null));
  }

  public void setLoadingStatus(LoadingStatusEnum status, Throwable throwable) {
    String error = throwable == null ? null : throwable.getLocalizedMessage();
    if (throwable != null) {
      throwable.printStackTrace();
    }
    loadingStatus.postValue(new ResponseWithError<>(status, error));
  }

  public void removeAccount(RedditAccount removedAccount) {
    dataModel.getUserRepo().removeAccount(removedAccount);
    dataModel.getMessageRepo().removeAccountMessages(removedAccount);
    if (currentAccount.getValue() != null) {
      /*
      If the current account is the same as the account being removed, then switch to the first
      account that isn't being removed. If there are no accounts left, set current username null
      to reset shared prefs and go back to login activity.
       */
      if (currentAccount.getValue().getUsername().equals(removedAccount.getUsername())) {
        dataModel.getUserRepo().getAccounts().observeForever(new Observer<List<RedditAccount>>() {
          @Override
          public void onChanged(@Nullable List<RedditAccount> redditAccounts) {
            if (redditAccounts != null) {
              if (redditAccounts.size() == 0) {
                //no accounts, go home
                setCurrentUsername(Constants.USER_REMOVED);
              } else {
                for (RedditAccount listAccount : redditAccounts) { //Switch to first account that isn't removed account.
                  if (!listAccount.getUsername().equals(removedAccount.getUsername())) {
                    initAccountSwitch(listAccount);
                    break;
                  }
                }
              }
            }
          }
        });
      }
    }
  }

  public void setAccountIsNew(boolean isNew) {
    if (currentAccount.getValue() != null) {
      currentAccount.getValue().setAccountIsNew(isNew);
      dataModel.getUserRepo().updateAccount(currentAccount.getValue());
    }
  }

  public enum LoadingStatusEnum {
    LOADING,
    ERROR,
    DONE
  }

  public boolean shouldReturnToLoginScreen(String username) {
    return username != null && username.equals(Constants.USER_REMOVED);
  }
}
