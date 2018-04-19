package app.endershrooms.inboxforreddit3.net.model;

import java.util.List;

public class MessageResponseData {
  public String after;
  public String modhash;
  public String whitelist_status;
  public List<IndividualMessageResponseModel> children;
  public String before;

}
