package app.endershrooms.inboxforreddit3;

import android.text.format.DateUtils;
import android.util.Log;

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

  public static void debugLog(String tag, String log) {
    if (log.length() > 4000) {
      Log.d(tag, log.substring(0, 4000));
      debugLog(tag, log.substring(4000));
    } else {
      Log.d(tag, log);
    }
  }

}
