package app.endershrooms.inboxforreddit3.net;

import io.reactivex.Single;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.POST;

/**
 * Created by Travis on 3/27/2018.
 */

public interface RedditApiNonOauth {
  @FormUrlEncoded
  @POST("api/v1/access_token")
  Single<JSONLoginResponse> getAccessTokenFromCode(
      @Header("Authorization") String authorization,
      @FieldMap Authentication.Params params);
}
