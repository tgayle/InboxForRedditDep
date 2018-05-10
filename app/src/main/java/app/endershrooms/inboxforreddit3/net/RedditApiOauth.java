package app.endershrooms.inboxforreddit3.net;

import app.endershrooms.inboxforreddit3.net.model.MessagesJSONResponse;
import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by Travis on 3/25/2018.
 */

public interface RedditApiOauth {

  //  @GET("message/{where}?count={count}&limit={limit}&after={after}")
  @GET("message/{where}")
  Single<MessagesJSONResponse> getMessages(
      @Header("Authorization") String token,
      @Path("where") String where,
      @Query("count") int count,
      @Query("limit") int limit,
      @Query("after") String after);

  @GET("api/v1/me")
  Single<JsonMeResponse> getMe(@Header("Authorization") String token);
}
