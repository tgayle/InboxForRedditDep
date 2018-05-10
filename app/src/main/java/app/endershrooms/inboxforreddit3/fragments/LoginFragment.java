package app.endershrooms.inboxforreddit3.fragments;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import app.endershrooms.inboxforreddit3.Constants;
import app.endershrooms.inboxforreddit3.R;
import app.endershrooms.inboxforreddit3.models.reddit.ResponseWithError;
import app.endershrooms.inboxforreddit3.viewmodels.BaseLoginViewModel.LoginWebviewResult;
import app.endershrooms.inboxforreddit3.viewmodels.EntryLoginActivityViewModel;

public class LoginFragment extends Fragment {


  private OnWebviewChange webviewChangeListener;

  public LoginFragment() {
    // Required empty public constructor
  }

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
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    EntryLoginActivityViewModel viewModel = ViewModelProviders.of(getActivity()).get(EntryLoginActivityViewModel.class);

    final WebView loginWebView = (WebView) getView().findViewById(R.id.loginWebView);
    final ProgressBar progressBar = (ProgressBar) getView().findViewById(R.id.login_progress);
    final SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) getView().findViewById(R.id.login_webview_swiperefresh);

    swipeRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
      @Override
      public void onRefresh() {
        clearWebViewHistory(loginWebView);
        loginWebView.loadUrl(Constants.getOauthLoginLink(viewModel.getLoginAuthState()));
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

    loginWebView.setWebViewClient(new WebViewClient() {
      @Override
      public void onPageStarted(WebView view, String url, Bitmap favicon) {
        if (url.contains("code=")) {
          Log.v("loginWebView", "WebView URL: " + url);
          loginWebView.setVisibility(View.GONE);
          progressBar.setVisibility(View.VISIBLE);
        }
      }

      @Override
      public void onPageFinished(WebView view, String url) {
        webviewChangeListener.onPageFinished();
        ResponseWithError<Boolean, LoginWebviewResult> whatToDo = viewModel.processLoadedPage(url);

          if (whatToDo.getError() != null) {
            switch (whatToDo.getError()) {
              case ACCESS_DENIED:
                Snackbar
                    .make(view, "Access Denied! You can't continue without a logged in account!",
                        Snackbar.LENGTH_LONG).show();
                break;
              case INCORRECT_STATE:
                Log.v("OauthLogin", "States did not match before and after!");
                break;
              case UNKNOWN:
                break;
            }
        }
      }

    });

//https:stackoverflow.com/questions/28998241/how-to-clear-cookies-and-cache-of-webview-on-android-when-not-in-webview
    clearWebViewHistory(loginWebView);
    loginWebView.loadUrl(Constants.getOauthLoginLink(viewModel.getLoginAuthState()));

  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View v = inflater.inflate(R.layout.activity_login_web_view, container, false);
    Log.v("Fragment", "Started Login fragment!");
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
  }

  @Override
  public void onDetach() {
    super.onDetach();
  }

  interface OnWebviewChange {
    void onPageFinished();
  }
}
