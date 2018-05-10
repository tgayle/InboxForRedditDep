package app.endershrooms.inboxforreddit3.net.model;

import com.google.gson.annotations.SerializedName;

public class Data {
  //    String first_message;
  public String first_message_name;
  public String name;
  //    String id;
  public String subject;
  public String author;
  public String dest;
  @SerializedName("new")
  public boolean isNew;
  public String body_html;
  public long created_utc;
  public String distinguished;
}
