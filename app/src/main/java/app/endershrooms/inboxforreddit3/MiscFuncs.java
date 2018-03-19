package app.endershrooms.inboxforreddit3;

import android.text.format.DateUtils;

/**
 * Created by Travis on 1/22/2018.
 */

public class MiscFuncs {
  public static String getRelativeDateTime(long millis) {
    return DateUtils.getRelativeTimeSpanString(millis, System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS).toString();
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

}
