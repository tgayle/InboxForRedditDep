package app.endershrooms.inboxforreddit3.net;

import app.endershrooms.inboxforreddit3.account.Authentication;
import app.endershrooms.inboxforreddit3.net.model.JSONLoginResponse;
import app.endershrooms.inboxforreddit3.net.model.JsonMeResponse;
import app.endershrooms.inboxforreddit3.net.model.MessagesJSONResponse;
import io.reactivex.Observable;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by Travis on 3/25/2018.
 */

public interface RedditApi {

  //Removed count param since it's not necessary
  @GET("message/{where}")
  Observable<MessagesJSONResponse> getMessagesWithAfter(
      @Header("Authorization") String token,
      @Path("where") String where,
      @Query("limit") int limit,
      @Query("after") String after);

  @GET("message/{where}")
  Observable<MessagesJSONResponse> getMessagesWithBefore(
      @Header("Authorization") String token,
      @Path("where") String where,
      @Query("limit") int limit,
      @Query("before") String before);

  @GET("api/v1/me")
  Observable<JsonMeResponse> getMe(@Header("Authorization") String token);

  @FormUrlEncoded
  @POST("https://www.reddit.com/api/v1/access_token")
  Observable<JSONLoginResponse> getAccessTokenFromCode(
      @Header("Authorization") String authorization,
      @FieldMap Authentication.Params.AuthParams params);
}
