package app.endershrooms.inboxforreddit3;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.util.Log;
import app.endershrooms.inboxforreddit3.database.AppDatabase;
import app.endershrooms.inboxforreddit3.net.RedditApi;
import io.reactivex.schedulers.Schedulers;
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
  private RedditApi redditApiOauth;
  private RxJava2CallAdapterFactory rxCallAdapter = RxJava2CallAdapterFactory.createWithScheduler(
      Schedulers.io());

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
      OkHttpClient.Builder builder = new Builder();
      Interceptor interceptor = new Interceptor() {
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
      };
//          builder.addInterceptor(interceptor)
      client = builder.build();
    }
    return client;
  }

  private Retrofit getRetrofitOauth() {
    if (retrofitOauth == null) {
      retrofitOauth = new Retrofit.Builder()
          .baseUrl("https://oauth.reddit.com/")
          .client(client())
          .addConverterFactory(GsonConverterFactory.create())
          .addCallAdapterFactory(rxCallAdapter)
          .build();
    }
    return retrofitOauth;
  }

  public RedditApi getRedditApiOauth() {
    if (redditApiOauth == null) {
      redditApiOauth = getRetrofitOauth().create(RedditApi.class);
    }
    return redditApiOauth;
  }

}
