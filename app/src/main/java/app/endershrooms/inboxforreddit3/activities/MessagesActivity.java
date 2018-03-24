package app.endershrooms.inboxforreddit3.activities;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import app.endershrooms.inboxforreddit3.R;
import app.endershrooms.inboxforreddit3.RecyclerViewVerticalSpace;
import app.endershrooms.inboxforreddit3.Singleton;
import app.endershrooms.inboxforreddit3.adapters.MessagesRecyclerViewAdapter;
import app.endershrooms.inboxforreddit3.models.Message;
import app.endershrooms.inboxforreddit3.models.RedditAccount;
import com.jakewharton.rxbinding2.view.RxView;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Travis on 1/20/2018.
 */

public class MessagesActivity extends AppCompatActivity {
  RedditAccount currentUser;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_messages_with_drawer);
    getSupportActionBar().hide();

    currentUser = (RedditAccount) getIntent().getSerializableExtra("account");

    TextView userTv = (TextView) findViewById(R.id.username_tv);
    final RecyclerView messageRv = (RecyclerView) findViewById(R.id.message_rv);
    messageRv.setLayoutManager(new LinearLayoutManager(MessagesActivity.this));

    userTv.setText(String.format(getString(R.string.login_complete_welcome_user), currentUser.getUsername()));
    MessagesRecyclerViewAdapter rvadapt = new MessagesRecyclerViewAdapter(new ArrayList<>());
    messageRv.setAdapter(rvadapt);
    messageRv.addItemDecoration(new RecyclerViewVerticalSpace(16));


    final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
    ImageButton drawerExpandAccountsBtn = (ImageButton) findViewById(R.id.drawer_expandusers_btn);
    final RecyclerView drawerAccountSwitcher = (RecyclerView) findViewById(R.id.activity_messages_drawer_users_list);

    drawerAccountSwitcher.setLayoutManager(new LinearLayoutManager(MessagesActivity.this));

    getMessageRx(currentUser)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(message -> {
          rvadapt.addMessage(message);
        });

    RxView.clicks(drawerExpandAccountsBtn)
        .subscribe(voida -> {
          if (drawerAccountSwitcher.getVisibility() == View.GONE) {
            drawerAccountSwitcher.setVisibility(View.VISIBLE);
            drawerExpandAccountsBtn.setRotation(180f);
          } else {
            drawerAccountSwitcher.setVisibility(View.GONE);
            drawerExpandAccountsBtn.setRotation(0f);

          }
        });

    RxView.clicks(userTv)
        .subscribe(avoid -> {
          Observable.just(Singleton.getInstance().getDb().accounts().getAllAccounts())
              .subscribeOn(Schedulers.io())
              .observeOn(AndroidSchedulers.mainThread())
              .subscribe(account -> {
                account.forEach(acc -> {
                  Log.v("LoadAccounts", String.format("%s %s %s", acc.getUsername(), acc.getAccessToken(), acc.getRefreshToken()));
                });
              });
        });

  }

  Observable<Message> getMessageRx(RedditAccount acc) {
    Observable<Message> observable = Observable.create(new ObservableOnSubscribe<Message>() {
      @Override
      public void subscribe(ObservableEmitter<Message> emitter) throws Exception {
        int count = 0;
        int limit = 30;
        String after = "";

        Uri.Builder urlBuilder = new Uri.Builder()
            .scheme("https")
            .authority("oauth.reddit.com")
            .appendPath("message")
            .appendPath("inbox")
            .appendQueryParameter("count", String.valueOf(count))
            .appendQueryParameter("limit", String.valueOf(limit))
            .appendQueryParameter("after", after);

        Request request = new Request.Builder()
            .url(urlBuilder.build().toString())
            .header("Authorization", " bearer " + acc.getAccessToken())
            .build();

        Singleton.getInstance().client().newCall(request).enqueue(new Callback() {
          @Override
          public void onFailure(Call call, IOException e) {

          }

          @Override
          public void onResponse(Call call, Response response) throws IOException {
            String strResponse = response.body().string();
            debugLog("Message", "response was " + strResponse);
            List<Message> messages = new ArrayList<>();
            try {
              JSONObject obj = new JSONObject(strResponse).getJSONObject("data");
              String after = obj.getString("after");
              Log.v("getMessages", "after was returned as " + after);

              JSONArray JsonMessages = obj.getJSONArray("children");

              for (int i = 0; i < JsonMessages.length(); i++) {
                JSONObject thisMessage = JsonMessages.getJSONObject(i).getJSONObject("data");

                String name = thisMessage.optString("name");
                String parent_name = thisMessage.optString("first_message_name");
                String subject = thisMessage.optString("subject");
                String author = thisMessage.optString("author");
                String destination = thisMessage.optString("dest");
                boolean newMessage = thisMessage.optBoolean("new");

                String messageContent = thisMessage.optString("body_html");

                //https://www.reddit.com/r/redditdev/comments/3n3mv0/android_snudown_java_binding_now_available/cvkk5fq/
                messageContent = messageContent.replace("&lt;", "<")
                    .replace("&gt;", ">")
                    .replace("&quot;", "\"")
                    .replace("&apos;", "'")
                    .replace("&amp;", "&")
                    .replace("<li><p>", "<p>• ")
                    .replace("</li>", "<br>")
                    .replaceAll("<li.*?>", "•")
                    .replace("<p>", "<div>")
                    .replace("</p>", "</div>");

                messageContent = messageContent.substring(0, messageContent.lastIndexOf("\n"));

                long timeCreated = TimeUnit.SECONDS.toMillis(thisMessage.optLong("created_utc"));
                Log.v("Messages", String.format("%s added from %s", name, author));

                Message thisMsg = new Message(currentUser.getUsername(), name, parent_name, author,
                    destination, subject, messageContent,
                    timeCreated, newMessage);
                emitter.onNext(thisMsg);

              }
            } catch (JSONException e) {
              e.printStackTrace();
            }
          }
        });
      }
    });
    return observable;
  }


  public static void debugLog(String tag, String log) {
    if (log.length() > 4000) {
      Log.d(tag, log.substring(0, 4000));
      debugLog(tag, log.substring(4000));
    } else {
      Log.d(tag, log);
    }
  }

  @Override
  public void onBackPressed() {
    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
    if (drawer.isDrawerOpen(GravityCompat.START)) {
      drawer.closeDrawer(GravityCompat.START);
    } else {
      super.onBackPressed();
    }
  }
}