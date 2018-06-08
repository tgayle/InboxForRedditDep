package app.endershrooms.inboxforreddit3;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import app.endershrooms.inboxforreddit3.models.reddit.RedditAccount;

/**
 * Created by Travis on 1/22/2018.
 */

public class MiscFuncs {

  public static String getRelativeDateTime(long millis) {
    return DateUtils.getRelativeTimeSpanString(millis * 1000, System.currentTimeMillis(),
        0L, DateUtils.FORMAT_ABBREV_ALL).toString();
  }

  public static CharSequence trim(CharSequence s) {
    int start = 0;
    int end = s.length();
    while (start < end && Character.isWhitespace(s.charAt(start))) {
      start++;
    }

    while (end > start && Character.isWhitespace(s.charAt(end - 1))) {
      end--;
    }

    return s.subSequence(start, end);
  }

  public static String noTrailingwhiteLines(String text) {
    while (text.charAt(text.length() - 1) == '\n') {
      text = text.substring(0, text.length() - 1);
    }
    return text;
  }

  public static void smartScrollToTop(RecyclerView recyclerView, int itemsVisibleBeforeTeleportingToTop) {
    if (!(recyclerView.getLayoutManager() instanceof LinearLayoutManager)) return;
    LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

    int topItemPosition;
    //the position of where the top of the list is. This can be position 0 or items.size - 1 if the layout manager has stackFromEnd and reverseLayout set to true
    int itemVisibleForTeleportCheck;
    //the first or last item visible in the recyclerview

    boolean recyclerViewIsReversed = linearLayoutManager.getStackFromEnd() && linearLayoutManager.getReverseLayout();
    if (recyclerViewIsReversed) {
      topItemPosition = (recyclerView.getAdapter().getItemCount() - 1 >= 0) ? recyclerView.getAdapter().getItemCount() - 1 : recyclerView.getAdapter().getItemCount();
      itemVisibleForTeleportCheck = linearLayoutManager.findLastVisibleItemPosition();
    } else {
      topItemPosition = 0;
      itemVisibleForTeleportCheck = linearLayoutManager.findFirstVisibleItemPosition();
    }

    boolean shouldTeleportToTop = (recyclerViewIsReversed) ?
        itemVisibleForTeleportCheck > itemsVisibleBeforeTeleportingToTop :
        itemVisibleForTeleportCheck < itemsVisibleBeforeTeleportingToTop;
    if (shouldTeleportToTop) {
      recyclerView.smoothScrollToPosition(topItemPosition);
    } else {
      recyclerView.scrollToPosition(topItemPosition);
    }
  }

  public static boolean shouldCurrentAccountBeReplaced(RedditAccount currentAccount, RedditAccount newAccount) {
    return currentAccount == null || !currentAccount.getUsername().equals(newAccount.getUsername());
  }

  public static void debugLog(String tag, String log) {
    if (log.length() > 4000) {
      Log.d(tag, log.substring(0, 4000));
      debugLog(tag, log.substring(4000));
    } else {
      Log.d(tag, log);
    }
  }

}
