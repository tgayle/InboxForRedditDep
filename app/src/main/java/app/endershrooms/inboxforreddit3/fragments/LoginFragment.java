package app.endershrooms.inboxforreddit3.fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import app.endershrooms.inboxforreddit3.BuildConfig;
import app.endershrooms.inboxforreddit3.R;
import java.util.UUID;

public class LoginFragment extends Fragment {

  String RESPONSE_TYPE = "code";
  String STATE = UUID.randomUUID().toString();

  public static final String CLIENT_ID = "***REMOVED***";
  public static final String OAUTH_REDIRECT_URI = "http://www.google.com";
  public static final String BASE_REDDIT_OAUTH = "https://oauth.reddit.com";


  public static final String OAUTH_SCOPE = "identity,edit,flair,history," +
      "modconfig,modflair,modlog," +
      "modposts,modwiki,mysubreddits," +
      "privatemessages,read,report," +
      "save,submit,subscribe,vote," +
      "wikiedit,wikiread";


  public static final String OAUTH_DURATION = "permanent";

  String OAUTHlink =
      "https://www.reddit.com/api/v1/authorize.compact?client_id=" + CLIENT_ID +
          "&response_type=" + RESPONSE_TYPE +
          "&state=" + STATE +
          "&redirect_uri=" + OAUTH_REDIRECT_URI +
          "&duration=" + OAUTH_DURATION +
          "&scope=" + OAUTH_SCOPE;

  private OnLoginCompleted loginListener;
  private OnWebviewChange  webviewChangeListener;

  public LoginFragment() {
    // Required empty public constructor
  }

  // TODO: Rename and change types and number of parameters
  public static LoginFragment newInstance() {
    LoginFragment fragment = new LoginFragment();
    Bundle args = new Bundle();

    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (getArguments() != null) {

    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View v = inflater.inflate(R.layout.activity_login_web_view, container, false);

    Log.v("Fragment", "Started Login fragment!");

    final WebView loginWebView = (WebView) v.findViewById(R.id.loginWebView);
    final ProgressBar progressBar = (ProgressBar) v.findViewById(R.id.login_progress);
    final SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.login_webview_swiperefresh);

    swipeRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
      @Override
      public void onRefresh() {
        clearWebViewHistory(loginWebView);
        loginWebView.loadUrl(OAUTHlink);
        Log.v("Login", "refresh started?");
        swipeRefreshLayout.setRefreshing(true);
      }
    });

    webviewChangeListener = new OnWebviewChange() {
      @Override
      public void onPageFinished() {
        Log.v("Login", "page finished load?");
        swipeRefreshLayout.setRefreshing(false);
      }
    };

//https:stackoverflow.com/questions/28998241/how-to-clear-cookies-and-cache-of-webview-on-android-when-not-in-webview
    clearWebViewHistory(loginWebView);

    loginWebView.setWebViewClient(new WebViewClient() {
      @Override
      public void onPageStarted(WebView view, String url, Bitmap favicon) {
        if (url.contains("code=")) {
          if (BuildConfig.DEBUG) {
            Log.v("loginWebView", "WebView URL: " + url);
          }
          loginWebView.setVisibility(View.GONE);
          progressBar.setVisibility(View.VISIBLE);
        }
      }

      @Override
      public void onPageFinished(WebView view, String url) {
        webviewChangeListener.onPageFinished();

        if (url.contains("&gws_rd=ssl") || url.contains("?state=" + STATE)) {
            Log.v("OAuthLogin", "URLopf is " + loginWebView.getUrl());
          final String redirect_uri = loginWebView.getUrl().replace("https://www.google.com/?", "");
          String[] params = redirect_uri.split("&");

          String redirect_state = params[0].replace("state=", "");
          String redirect_code = params[1].replace("code=", "");

          if (!STATE.equalsIgnoreCase(redirect_state)) {
            if (BuildConfig.DEBUG) {
              Log.v("OauthLogin", "States did not match before and after!");
            }
          } else {
            if (BuildConfig.DEBUG) {
              Log.v("OauthLogin", "States did match! Continuing!");
            }
          }

          if (url.contains("access_denied")) {
            Snackbar.make(view, "Access Denied! You can't continue without a logged in account!",
                Snackbar.LENGTH_LONG).show();
          }

          loginListener.loginCompleted(redirect_code);

        }
      }

    });
    loginWebView.loadUrl(OAUTHlink);

    return v;
  }

  private void clearWebViewHistory(WebView webview) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
      CookieManager.getInstance().removeAllCookies(null);
      CookieManager.getInstance().flush();
    } else {
      CookieSyncManager cookieSyncMngr = CookieSyncManager.createInstance(getContext());
      cookieSyncMngr.startSync();
      CookieManager cookieManager = CookieManager.getInstance();
      cookieManager.removeAllCookie();
      cookieManager.removeSessionCookie();
      cookieSyncMngr.stopSync();
      cookieSyncMngr.sync();
    }
    //////////////////////////////////////////////////////////////
    webview.clearCache(true);
    webview.clearHistory();
    WebSettings webSettings = webview.getSettings();
    webSettings.setSaveFormData(false);
    webSettings.setSavePassword(false);
  }


  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    if (context instanceof OnLoginCompleted) {
      loginListener = (OnLoginCompleted) context;
    } else {
      throw new RuntimeException(context.toString()
          + " must implement OnFragmentInteractionListener");
    }
  }

  @Override
  public void onDetach() {
    super.onDetach();
    loginListener = null;
  }

  public interface OnLoginCompleted {
    void loginCompleted(String code);
  }

  interface OnWebviewChange {
    void onPageFinished();
  }
}
