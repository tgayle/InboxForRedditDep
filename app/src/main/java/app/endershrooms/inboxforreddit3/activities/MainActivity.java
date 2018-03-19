package app.endershrooms.inboxforreddit3.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import app.endershrooms.inboxforreddit3.Constants;
import app.endershrooms.inboxforreddit3.R;
import app.endershrooms.inboxforreddit3.Singleton;
import app.endershrooms.inboxforreddit3.adapters.WelcomeActivityViewPagerAdapter;
import app.endershrooms.inboxforreddit3.fragments.LoginFragment.OnLoginCompleted;
import app.endershrooms.inboxforreddit3.interfaces.StartLogin;
import app.endershrooms.inboxforreddit3.views.NoSwipeViewPager;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static app.endershrooms.inboxforreddit3.Constants.BASE_REDDIT_OAUTH;

public class MainActivity extends AppCompatActivity implements OnLoginCompleted, StartLogin {

    NoSwipeViewPager viewPager;
    WelcomeActivityViewPagerAdapter vpAdapter;
    private List<LoginUpdateListener> mListeners = new ArrayList<>();
    OkHttpClient client = Singleton.getInstance().client();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        viewPager = (NoSwipeViewPager) findViewById(R.id.vpager);
        viewPager.setPagingEnabled(false);
        vpAdapter = new WelcomeActivityViewPagerAdapter(getSupportFragmentManager());

        viewPager.setAdapter(vpAdapter);
        viewPager.setCurrentItem(0);
    }


    @Override
    public void loginCompleted(String code) {
        viewPager.setCurrentItem(2);

        Request.Builder buildAccessTokenRequest = new Request.Builder()
                .url("https://www.reddit.com/api/v1/access_token");

        updateLoadingTextProgress("Preparing token params");
        HashMap<String, String> tokenParams = new HashMap<>();
        tokenParams.put("grant_type", "authorization_code");
        tokenParams.put("code", code);
        tokenParams.put("redirect_uri", Constants.OAUTH_REDIRECT_URI);

        updateLoadingTextProgress("Preparing headers");
        HashMap<String,String> headers = new HashMap<>();
        String creds = Constants.CLIENT_ID + ":" + "";
        headers.put("Authorization", "Basic " + Base64.encodeToString(creds.getBytes(), Base64.NO_WRAP));


        MultipartBody.Builder body = new MultipartBody.Builder().setType(MultipartBody.FORM);
        for (Map.Entry<String, String> para: tokenParams.entrySet()) {
            body.addFormDataPart(para.getKey(), para.getValue());
        }

        for (Map.Entry<String, String> head: headers.entrySet()) {
            buildAccessTokenRequest.header(head.getKey(), head.getValue());
        }

        buildAccessTokenRequest.post(body.build());
        Request request = buildAccessTokenRequest.build();

        updateLoadingTextProgress("Getting access token.");
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String strResponse = response.body().string();
                Log.v("Download access token", strResponse);

                try {
                    JSONObject jsonResponse = new JSONObject(strResponse);
                    final String accessToken = jsonResponse.getString("access_token");

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateLoadingTextProgress("Success getting access token!");

                            updateLoadingTextProgress("Getting user info.");
                        }
                    });

                    {

                        Request buildGetUserInfo = new Request.Builder()
                                .url(BASE_REDDIT_OAUTH + "/api/v1/me")
                                .header("Authorization", " bearer " + accessToken)
                                .build();

                        client.newCall(buildGetUserInfo).enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {

                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                String strResponse = response.body().string();
                                Log.v("UserInfo", strResponse);
                                try {
                                    final String username = new JSONObject(strResponse).getString("name");

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            updateLoadingTextProgress("Hello " + username + "!");
                                        }
                                    });

                                    MainActivity.this.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            goToMessagesActivity(username, accessToken);
                                        }
                                    });




                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        });

    }

    public synchronized void registerDataUpdateListener(LoginUpdateListener listener) {
        mListeners.add(listener);
    }

    public synchronized void unregisterDataUpdateListener(LoginUpdateListener listener) {
        mListeners.remove(listener);
    }

    public synchronized void updateLoadingTextProgress(String text) {
        for (LoginUpdateListener listener : mListeners) {
            listener.updateLoadingText(text);
        }
    }

    void goToMessagesActivity(final String user, final String accessToken) {
        new Handler()
                .postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent i = new Intent(MainActivity.this, MessagesActivity.class);
                        i.putExtra("user", user);
                        i.putExtra("access_token", accessToken);
                        startActivity(i);

                    }
                }, 2500);
    }

    @Override
    public void startLogin() {
        viewPager.setCurrentItem(1);
    }

    public interface LoginUpdateListener {
        void updateLoadingText(String text);
    }

}
