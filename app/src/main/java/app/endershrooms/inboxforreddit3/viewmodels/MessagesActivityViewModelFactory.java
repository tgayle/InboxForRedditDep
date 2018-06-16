package app.endershrooms.inboxforreddit3.viewmodels;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;
import app.endershrooms.inboxforreddit3.viewmodels.model.MessagesActivityDataModel;
import app.endershrooms.inboxforreddit3.viewmodels.model.MessagesControllerViewModel;

public class MessagesActivityViewModelFactory extends ViewModelProvider.NewInstanceFactory {
  private MessagesActivityDataModel dataModel;

  public MessagesActivityViewModelFactory(MessagesActivityDataModel dataModel) {
    this.dataModel = dataModel;
  }

  @NonNull
  @Override
  public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
    return (T) new MessagesControllerViewModel(dataModel);
  }
}
