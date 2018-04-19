package app.endershrooms.inboxforreddit3.net.model;

import android.util.Log;
import app.endershrooms.inboxforreddit3.models.Message;
import app.endershrooms.inboxforreddit3.models.RedditAccount;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Travis on 3/25/2018.
 */

public class MessagesJSONResponse {
  public String kind;
  public MessageResponseData data;

  @Deprecated
  public List<Message> convertJsonToMessages(RedditAccount currentUser) {
    List<Message> messageList= new ArrayList<>();
    for (IndividualMessageResponseModel child : data.children) {
      Data message = child.data;
//      Log.d("JSONMessages", String.format("Name: %s%n"
//          + "Parent: %s%n"
//          + "Timestamp: %s%n"
//          + "Author: %s%n"
//          + "Dest: %s%n", message.name, message.first_message_name, message.created_utc, message.author, message.dest));

      Message properMsg = new Message(currentUser, message.name, message.first_message_name, message.author, message.dest, message.subject, message.body_html, message.created_utc, message.isNew);
      messageList.add(properMsg);
    }
    return messageList;
  }

  public List<Message> otherConvertJsonToMessages(RedditAccount currentUser) {
    List<Message> messages = new ArrayList<>();
    for (IndividualMessageResponseModel child : data.children) {
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

      Message thisMsg = new Message(currentUser, name, parent_name, messageData.author, messageData.dest, messageData.subject, messageContent, messageData.created_utc, messageData.isNew);
      messages.add(thisMsg);
    }
    return messages;
  }

  public void printJsonDebugMessages() {
    for (IndividualMessageResponseModel child : data.children) {
      Data message = child.data;
      Log.d("JSONMessages", String.format("Name: %s%n"
          + "Parent: %s%n"
          + "Timestamp: %s%n"
          + "Author: %s%n"
          + "Dest: %s%n", message.name, message.first_message_name, message.created_utc, message.author, message.dest));
    }
  }

}

