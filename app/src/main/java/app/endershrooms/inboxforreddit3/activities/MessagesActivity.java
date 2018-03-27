package app.endershrooms.inboxforreddit3.activities;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
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
import app.endershrooms.inboxforreddit3.adapters.AccountsListAdapter;
import app.endershrooms.inboxforreddit3.adapters.MessagesConversationRecyclerViewAdapter;
import app.endershrooms.inboxforreddit3.models.Conversation;
import app.endershrooms.inboxforreddit3.models.Message;
import app.endershrooms.inboxforreddit3.models.RedditAccount;
import app.endershrooms.inboxforreddit3.net.model.Data;
import app.endershrooms.inboxforreddit3.net.model.IndividualMessageResponseModel;
import app.endershrooms.inboxforreddit3.net.model.MessagesJSONResponse;
import com.jakewharton.rxbinding2.view.RxView;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
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
  MessagesConversationRecyclerViewAdapter messageConversationAdapter = new MessagesConversationRecyclerViewAdapter(new ArrayList<>());
  AccountsListAdapter accountsListAdapter = new AccountsListAdapter(new ArrayList<>());

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_messages_with_drawer);
    getSupportActionBar().hide();

    currentUser = (RedditAccount) getIntent().getSerializableExtra("account");

    TextView userTv = (TextView) findViewById(R.id.username_tv);

    //TODO: Implement swipe refresh.

    userTv.setText(String.format(getString(R.string.login_complete_welcome_user), currentUser.getUsername()));

    final RecyclerView messageRv = (RecyclerView) findViewById(R.id.message_rv);
    messageRv.setLayoutManager(new LinearLayoutManager(MessagesActivity.this));
    messageRv.setAdapter(messageConversationAdapter);
    ((LinearLayoutManager) messageRv.getLayoutManager()).setReverseLayout(true);
    ((LinearLayoutManager) messageRv.getLayoutManager()).setStackFromEnd(true);

    final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
    ImageButton drawerExpandAccountsBtn = (ImageButton) findViewById(R.id.drawer_expandusers_btn);

    final RecyclerView drawerAccountSwitcher = (RecyclerView) findViewById(R.id.activity_messages_drawer_users_list);
    drawerAccountSwitcher.setLayoutManager(new LinearLayoutManager(MessagesActivity.this));
    drawerAccountSwitcher.setAdapter(accountsListAdapter);
    drawerAccountSwitcher.addItemDecoration(new RecyclerViewVerticalSpace(16));

    Singleton.get().getDb().accounts().getAllAccounts()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(accounts -> {
          for (RedditAccount account : accounts) {
            Log.v("MessagesActivity", account.loggingInfo());
          }
          accountsListAdapter.addAccounts(accounts);
        });

    updateConversationsList();

    SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.activities_messages_swiperefresh);
    swipeRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
      @Override
      public void onRefresh() {
        messageConversationAdapter.animateRemoval();
//        getMessageRx(currentUser, "inbox") //Update messages.
//            .subscribeOn(Schedulers.io())
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribe();
//
//        getMessageRx(currentUser, "sent")
//            .subscribeOn(Schedulers.io())
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribe(messages -> {
//              updateConversationsList();
//              Log.v("updateMsg", messages.get(0).getMessageBody());
//              swipeRefreshLayout.setRefreshing(false);
//            });
      }
    });

    Single.fromCallable(() -> getMessages(currentUser, "inbox"))
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(ignored -> {
          Single.fromCallable(() -> getMessages(currentUser, "sent"))
              .subscribeOn(Schedulers.io())
              .observeOn(AndroidSchedulers.mainThread())
              .subscribe();
        });

//    getMessageRx(currentUser, "inbox") //Update messages.
//        .subscribeOn(Schedulers.io())
//        .observeOn(AndroidSchedulers.mainThread())
//        .subscribe();
//
//    getMessageRx(currentUser, "sent") //Update messages.
//        .subscribeOn(Schedulers.io())
//        .observeOn(AndroidSchedulers.mainThread())
//        .subscribe();

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
          Singleton.get().getDb().accounts().getAllAccounts()
              .subscribeOn(Schedulers.io())
              .observeOn(AndroidSchedulers.mainThread())
              .subscribe(accounts -> {
                for (RedditAccount acc : accounts) {
                  Log.v("LoadAccounts", String.format("%s %s %s", acc.getUsername(), acc.getAccessToken(), acc.getRefreshToken()));
                }
              });
        });

  }

  private void updateConversationsList() {
    Single.fromCallable(() -> Singleton.get().getDb().messages().getAllUserMessagesAsc(currentUser.getUsername()))
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(messages -> {
          Single.fromCallable(() -> Conversation.separateMessagesIntoConversations(messages))
              .subscribeOn(Schedulers.computation())
              .observeOn(AndroidSchedulers.mainThread())
              .subscribe(conversations -> {
                Log.v("update", "called");
                messageConversationAdapter.addConversations(conversations);
              });
        });
  }

  List<Message> getMessages(RedditAccount acc, String where) {
    List<Message> messages = new ArrayList<>();
    Singleton.get().getRedditApiOauth().getMessages(acc.getAuthentication(), where, 0, 30, "")
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(messagesJSONResponse -> {
          //look to putting on computation thread
          for (IndividualMessageResponseModel child : messagesJSONResponse.data.children) {
            String name = child.data.name;
            String parent_name = child.data.first_message_name;
            if (parent_name == null || parent_name.equals("null")) parent_name = name;

            Data messageData = child.data;

            String messageContent = child.data.body_html.replace("&lt;", "<")
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

            Message thisMsg = new Message(currentUser.getUsername(), name, parent_name, messageData.author, messageData.dest, messageData.subject, messageContent, messageData.created_utc, messageData.isNew);
            messages.add(thisMsg);
          }

          Completable.fromAction(() -> Singleton.get().getDb().messages().insertMessages(messages))
              .subscribeOn(Schedulers.io())
              .observeOn(AndroidSchedulers.mainThread())
              .subscribe();

          Single.fromCallable(() -> Conversation.separateMessagesIntoConversations(messages))
              .subscribeOn(Schedulers.computation())
              .observeOn(AndroidSchedulers.mainThread())
              .subscribe(conversations -> {
                messageConversationAdapter.addConversations(conversations);
              });
        });
    return messages;
  }

  Single<Single<MessagesJSONResponse>> updateMessages(RedditAccount account, String where) {
    return Single.fromCallable(() -> Singleton.get().getRedditApiOauth().getMessages(account.getAuthentication(), where, 0, 30, ""))
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread());
  }


  Observable<List<Message>> getMessageRx(RedditAccount acc, String where) {
    return Observable.create(emitter -> {
      int count = 0;
      int limit = 30;
      String after = "";

      //Where can be "inbox" or "sent"
      //TODO: Load all past messages, not just 30.
      Uri.Builder urlBuilder = new Uri.Builder()
          .scheme("https")
          .authority("oauth.reddit.com")
          .appendPath("message")
          .appendPath(where)
          .appendQueryParameter("count", String.valueOf(count))
          .appendQueryParameter("limit", String.valueOf(limit))
          .appendQueryParameter("after", after);

      Request request = new Request.Builder()
          .url(urlBuilder.build().toString())
          .header("Authorization", " bearer " + acc.getAccessToken())
          .build();

      Singleton.get().client().newCall(request).enqueue(new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {

        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
          String strResponse = response.body().string();
          debugLog("Message", "response was " + strResponse);
          List<Message> messages = new ArrayList<>();
          try {
            JSONObject baseObj = new JSONObject(strResponse);

            if (baseObj.optInt("error", 0) == 401) {
              //Update token necessary.

            }

            JSONObject obj = baseObj.getJSONObject("data");


            String after = obj.getString("after");
            Log.v("getMessages", "after was returned as " + after);

            JSONArray JsonMessages = obj.getJSONArray("children");

            for (int i = 0; i < JsonMessages.length(); i++) {
              JSONObject thisMessage = JsonMessages.getJSONObject(i).getJSONObject("data");

              String name = thisMessage.optString("name");
              String parent_name = thisMessage.getString("first_message_name");
              if (parent_name.equals("null")) parent_name = name;
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
//              Log.v("Messages", String.format("%s added from %s", name, messageContent));

              Message thisMsg = new Message(currentUser.getUsername(), name, parent_name, author,
                  destination, subject, messageContent,
                  timeCreated, newMessage);
              messages.add(thisMsg);
            }
          } catch (JSONException e) {
            e.printStackTrace();
          }
          Singleton.get().getDb().messages().insertMessages(messages);
          emitter.onNext(messages);
        }
      });
    }
    );
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