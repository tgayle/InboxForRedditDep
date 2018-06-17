package app.endershrooms.inboxforreddit3.viewmodels.model;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.Nullable;
import android.util.Log;
import app.endershrooms.inboxforreddit3.models.reddit.Message;
import app.endershrooms.inboxforreddit3.models.reddit.RedditAccount;
import app.endershrooms.inboxforreddit3.models.reddit.ResponseWithError;
import app.endershrooms.inboxforreddit3.viewmodels.MessagesActivityViewModel.LoadingStatusEnum;
import java.util.List;

public class MessagesControllerViewModel extends ViewModel {

  private MessagesActivityDataModel dataModel;
  private LiveData<RedditAccount> currentAccount;
  private MutableLiveData<ResponseWithError<LoadingStatusEnum, String>> loadingStatus = new MutableLiveData<>();

  public MessagesControllerViewModel(MessagesActivityDataModel dataModel) {
    this.dataModel = dataModel;
    currentAccount = dataModel.getCurrentAccount();
  }


  public LiveData<Boolean> markAllMessagesAsRead() {
    return dataModel.markAllUnreadMessagesAsRead();
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

  public LiveData<ResponseWithError<String, Throwable>> loadNewestMessages() {
    Log.d("MessagesViewModel", "Load newest " + (currentAccount.getValue() == null ? currentAccount : currentAccount.getValue().getUsername()));
    return dataModel.getMessageRepo().loadNewestMessages(currentAccount.getValue());
  }

  public void deleteMessages(List<Message> selectedItems) {
    dataModel.getMessageRepo().hideMessages(selectedItems);
  }
}
