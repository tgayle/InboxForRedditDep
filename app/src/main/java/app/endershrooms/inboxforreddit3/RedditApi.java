package app.endershrooms.inboxforreddit3;

/**
 * Created by Travis on 3/25/2018.
 */

public class RedditApi {

//  public void updateAccountAuthentication(RedditAccount account) {
//    if (account.getTokenExpirationDate() - System.currentTimeMillis() < 0) {
//      //token hasn't expired yet.
//    } else {
//      Uri.Builder urlBuilder = new Uri.Builder()
//          .scheme("https")
//          .authority("www.reddit.com")
//          .appendPath("api")
//          .appendPath("v1")
//          .appendPath("access_token");
//
//      String creds = Constants.CLIENT_ID + ":" + "";
//      Request.Builder request = new Request.Builder()
//          .url(urlBuilder.build().toString())
//          .header("Authorization", "Basic " + Base64.encodeToString(creds.getBytes(), Base64.NO_WRAP));
//
//      HashMap<String, String> tokenParams = new HashMap<>();
//      tokenParams.put("grant_type", "refresh_token");
//      tokenParams.put("refresh_token", account.getRefreshToken());
//
//      HashMap<String, String> headers = new HashMap<>();
//      headers
//          .put("Authorization", "Basic " + Base64.encodeToString(creds.getBytes(), Base64.NO_WRAP));
//
//      MultipartBody.Builder body = new MultipartBody.Builder().setType(MultipartBody.FORM);
//      for (Entry<String, String> para : tokenParams.entrySet()) {
//        body.addFormDataPart(para.getKey(), para.getValue());
//      }
//
//      Singleton.get().client().newCall(request.build()).enqueue(new Callback() {
//        @Override
//        public void onFailure(Call call, IOException e) {
//
//        }
//
//        @Override
//        public void onResponse(Call call, ResponseWithError response) throws IOException {
//          String rsp = response.body().string();
//          Log.d("RedditApiOauth", rsp);
//        }
//      });
//    }
//  }

}
