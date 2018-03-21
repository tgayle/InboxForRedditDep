package app.endershrooms.inboxforreddit3;

import static app.endershrooms.inboxforreddit3.Constants.BASE_REDDIT_OAUTH;

import android.content.Context;
import android.util.Base64;
import android.util.Log;
import app.endershrooms.inboxforreddit3.activities.MainActivity.LoginUpdateListener;
import app.endershrooms.inboxforreddit3.models.RedditAccount;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.Response;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Travis on 3/20/2018.
 */

public class Login {

  private RedditAccount account;
  private Context context;
  private static OkHttpClient client = Singleton.getInstance().client();

  public Login(String redirectCode, Context context) {
    this(redirectCode, context,  null);
  }

  public Login(String redirectCode, final Context context, final LoginUpdateListener loginProgressListener) {
    this.context = context;

    Builder buildAccessTokenRequest = new Builder()
        .url("https://www.reddit.com/api/v1/access_token");

    loginProgressListener.updateLoadingText("Preparing token params");
    HashMap<String, String> tokenParams = new HashMap<>();
    tokenParams.put("grant_type", "authorization_code");
    tokenParams.put("code", redirectCode);
    tokenParams.put("redirect_uri", Constants.OAUTH_REDIRECT_URI);

    loginProgressListener.updateLoadingText("Preparing headers");
    HashMap<String, String> headers = new HashMap<>();
    String creds = Constants.CLIENT_ID + ":" + "";
    headers
        .put("Authorization", "Basic " + Base64.encodeToString(creds.getBytes(), Base64.NO_WRAP));

    MultipartBody.Builder body = new MultipartBody.Builder().setType(MultipartBody.FORM);
    for (Entry<String, String> para : tokenParams.entrySet()) {
      body.addFormDataPart(para.getKey(), para.getValue());
    }

    for (Entry<String, String> head : headers.entrySet()) {
      buildAccessTokenRequest.header(head.getKey(), head.getValue());
    }

    buildAccessTokenRequest.post(body.build());
    Request request = buildAccessTokenRequest.build();

    loginProgressListener.updateLoadingText("Getting access token.");
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
          final String refreshToken = jsonResponse.getString("refresh_token");

              loginProgressListener.updateLoadingText(context.getString(R.string.hello_blank_fragment));

              loginProgressListener.updateLoadingText("Getting user info.");

          {

            Request buildGetUserInfo = new Builder()
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
                  account = new RedditAccount(username, accessToken, refreshToken);

                  loginProgressListener.updateLoadingText(String.format(context.getString(R.string.login_complete_hello_user), username));
                  loginProgressListener.onCompleteLogin(account);


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

  public RedditAccount getAccount() {
    return account;
  }
}
