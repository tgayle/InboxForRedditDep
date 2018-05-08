package app.endershrooms.inboxforreddit3;

import android.net.Uri;

/**
 * Created by Travis on 1/19/2018.
 */

public class Constants {

  public static final String CLIENT_ID = "***REMOVED***";
  public static final String OAUTH_REDIRECT_URI = "http://www.google.com";
  public static final String BASE_REDDIT_OAUTH = "https://oauth.reddit.com";


  public static final String OAUTH_SCOPE = "identity,edit,flair,history," +
      "modconfig,modflair,modlog," +
      "modposts,modwiki,mysubreddits," +
      "privatemessages,read,report," +
      "save,submit,subscribe,vote," +
      "wikiedit,wikiread";

  public static final String RESPONSE_TYPE = "code";
  public static final String OAUTH_DURATION = "permanent";

  public static final String TAB_MESSAGES = "Inbox";
  public static final String TAB_UNREAD = "Unread";
  public static final String TAB_SENT = "Sent";

  public static final String SHARED_PREFERENCES_MAIN = "inbox_for_reddit_main";
  public static final String SHARED_PREFS_CURRENT_ACC = "current_account";

  public static String getOauthLoginLink(String state) {
    return Uri.parse("https://www.reddit.com/api/v1/authorize.compact?")
        .buildUpon()
        .appendQueryParameter("client_id", CLIENT_ID)
        .appendQueryParameter("response_type", RESPONSE_TYPE)
        .appendQueryParameter("state", state)
        .appendQueryParameter("redirect_uri", OAUTH_REDIRECT_URI)
        .appendQueryParameter("duration", OAUTH_DURATION)
        .appendQueryParameter("scope", OAUTH_SCOPE)
        .build().toString();
  }

}