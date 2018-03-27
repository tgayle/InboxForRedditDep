package app.endershrooms.inboxforreddit3;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.util.Log;
import app.endershrooms.inboxforreddit3.database.AppDatabase;
import app.endershrooms.inboxforreddit3.net.RedditApiNonOauth;
import app.endershrooms.inboxforreddit3.net.RedditApiOauth;
import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.Builder;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Travis on 1/22/2018.
 */

public class Singleton {

  private static Singleton thisSingleton;
  private AppDatabase db;
  private OkHttpClient client;
  private Retrofit retrofitOauth;
  private Retrofit retrofitNonOauth;
  private RedditApiOauth redditApiOauth;
  private RedditApiNonOauth redditApiNonOauth;


  public static Singleton get() {
    if (thisSingleton == null) {
      thisSingleton = new Singleton();
    }
    return thisSingleton;
  }

  public AppDatabase prepareDatabase(Context context) {
    if (db == null) {
      db = Room.databaseBuilder(context, AppDatabase.class, "InboxForRedditDB.db").build();
    }
    return db;
  }

  public AppDatabase getDb() {
    return db;
  }

  public OkHttpClient client() {
    if (client == null) {
      client = new Builder()
          .addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
              final Request original = chain.request();
              final HttpUrl originalHttpUrl = original.url();
              final Response response = chain.proceed(original);
              Log.v("ClientWork", originalHttpUrl.toString());
              for (Entry<String, List<String>> stringListEntry : original.headers().toMultimap()
                  .entrySet()) {
                Log.v("ClientWork", String.format("%s -> %s", stringListEntry.getKey(), stringListEntry.getValue()));
              }
//              Log.v("ClientWork", original.body().toString());
//              Log.v("ClientWork", response.body().string());
//              Log.v("ClientWork", response.code() + "");
              return response;
            }
          }).build();
    }
    return client;
  }

  public Retrofit getRetrofitOauth() {
    if (retrofitOauth == null) {
      retrofitOauth = new Retrofit.Builder()
          .baseUrl("https://oauth.reddit.com/")
          .client(client())
          .addConverterFactory(GsonConverterFactory.create())
          .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
          .build();
    }
    return retrofitOauth;
  }

  public Retrofit getRetrofitNonOauth() {
    if (retrofitNonOauth == null) {
      retrofitNonOauth = new Retrofit.Builder()
          .baseUrl("https://www.reddit.com/")
          .client(client())
          .addConverterFactory(GsonConverterFactory.create())
          .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
          .build();
    }
    return retrofitNonOauth;
  }

  public RedditApiOauth getRedditApiOauth() {
    if (redditApiOauth == null) {
      redditApiOauth = getRetrofitOauth().create(RedditApiOauth.class);
    }
    return redditApiOauth;
  }

  public RedditApiNonOauth getRedditApiNonOauth() {
    if (redditApiNonOauth == null) {
      redditApiNonOauth = getRetrofitNonOauth().create(RedditApiNonOauth.class);
    }
    return redditApiNonOauth;
  }

}
