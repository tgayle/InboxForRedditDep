package app.endershrooms.inboxforreddit3.interfaces;

import app.endershrooms.inboxforreddit3.models.reddit.RedditAccount;

/**
 * Created by Travis on 5/9/2018.
 */

public interface OnAccountListInteraction {

  void onAccountSelected(RedditAccount account);
  void onAccountRemoved(RedditAccount account);
}
