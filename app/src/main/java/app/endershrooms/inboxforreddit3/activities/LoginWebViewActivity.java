package app.endershrooms.inboxforreddit3.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import java.util.UUID;

import app.endershrooms.inboxforreddit3.BuildConfig;
import app.endershrooms.inboxforreddit3.R;


/**
 * Created on 12/16/2017.
 */

public class LoginWebViewActivity extends AppCompatActivity {

    String RESPONSE_TYPE = "code";
    String STATE = UUID.randomUUID().toString();;

    public static final String CLIENT_ID          = "***REMOVED***";
    public static final String OAUTH_REDIRECT_URI = "http://www.google.com";
    public static final String BASE_REDDIT_OAUTH        = "https://oauth.reddit.com";


    public static final String OAUTH_SCOPE        = "identity,edit,flair,history,"   +
            "modconfig,modflair,modlog,"     +
            "modposts,modwiki,mysubreddits," +
            "privatemessages,read,report,"   +
            "save,submit,subscribe,vote,"    +
            "wikiedit,wikiread";


    public static final String OAUTH_DURATION     = "permanent";

    String OAUTHlink =
            "https://www.reddit.com/api/v1/authorize.compact?client_id=" + CLIENT_ID +
                    "&response_type=" + RESPONSE_TYPE +
                    "&state=" + STATE +
                    "&redirect_uri=" + OAUTH_REDIRECT_URI +
                    "&duration=" + OAUTH_DURATION +
                    "&scope=" + OAUTH_SCOPE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_web_view);
        getSupportActionBar().hide();


        final WebView loginWebView = (WebView) findViewById(R.id.loginWebView);
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.login_progress);


        //https:

//stackoverflow.com/questions/28998241/how-to-clear-cookies-and-cache-of-webview-on-android-when-not-in-webview
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            CookieManager.getInstance().removeAllCookies(null);
            CookieManager.getInstance().flush();
        } else {
            CookieSyncManager cookieSyncMngr = CookieSyncManager.createInstance(getApplicationContext());
            cookieSyncMngr.startSync();
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.removeAllCookie();
            cookieManager.removeSessionCookie();
            cookieSyncMngr.stopSync();
            cookieSyncMngr.sync();
        }
        //////////////////////////////////////////////////////////////
        loginWebView.clearCache(true);
        loginWebView.clearHistory();
        WebSettings webSettings = loginWebView.getSettings();
        webSettings.setSaveFormData(false);
        webSettings.setSavePassword(false);

        loginWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                if (url.contains("code=")) {
                    if (BuildConfig.DEBUG) Log.v("loginWebView", "WebView URL: " + url);
                    loginWebView.setVisibility(View.GONE);
                    progressBar.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (url.contains("&gws_rd=ssl") || url.contains("?state=" + STATE)) {
                    if (BuildConfig.DEBUG)
                        Log.v("OAuthLogin", "URLopf is " + loginWebView.getUrl());
                    final String redirect_uri = loginWebView.getUrl().replace("https://www.google.com/?", "");
                    String[] params = redirect_uri.split("&");

                    String redirect_state = params[0].replace("state=", "");
                    String redirect_code = params[1].replace("code=", "");

                    if (!STATE.equalsIgnoreCase(redirect_state)) {
                        if (BuildConfig.DEBUG)
                            Log.v("OauthLogin", "States did not match before and after!");
                    } else {
                        if (BuildConfig.DEBUG) Log.v("OauthLogin", "States did match! Continuing!");
                    }

                    if (url.contains("access_denied")) {
                        Snackbar.make(view, "Access Denied! You can't continue without a logged in account!", Snackbar.LENGTH_LONG).show();
                    }

                    Intent intent = new Intent();
                    intent.putExtra("code", redirect_code);
                    setResult(Activity.RESULT_OK, intent);
                    finish();

                }
            }

        });
        loginWebView.loadUrl(OAUTHlink);
    }
}
