package app.endershrooms.inboxforreddit3.models;

import android.text.Html;
import android.util.Log;
import io.reactivex.Single;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Model class for a reddit conversation.
 * This is defined as a list of messages with the same subject, parent name and people involved.
 * Created by Travis on 3/24/2018.
 */

public class Conversation {

  private String parentName;
  private List<Message> messages;

  public Conversation(String parentName, List<Message> messages) {
    this.parentName = parentName;
    this.messages = messages;
  }

  public Conversation(String parentName, Message message) {
    this.parentName = parentName;
    messages = new ArrayList<>();
    messages.add(message);
  }

  public String getParentName() {
    return parentName;
  }

  public List<Message> getMessages() {
    return messages;
  }

  public void addMessage(Message message) {
    if (message.getParentMessageName().equals(parentName)) {
      this.messages.add(message);
    }
  }
//Conversations are returned from here where the most recent messages is the last conversation in the array.
  public static List<Conversation> separateMessagesIntoConversations(List<Message> messages) {
    List<String> parentNameList = new LinkedList<>();
    List<Conversation> conversations = new ArrayList<>();

    for (Message message : messages) {
      String parentName = message.getParentMessageName();
      if (parentNameList.contains(parentName)) {
        int conversationIndex = parentNameList.indexOf(parentName);
        Conversation oldConversation = conversations.get(conversationIndex);
        oldConversation.addMessage(message);
        conversations.set(conversationIndex, oldConversation);
      } else {
        parentNameList.add(parentName);
        conversations.add(new Conversation(parentName, message));
      }
      Log.d("separateMessage", String.format("%s -> %s", parentName, Html.fromHtml(message.getMessageBody()).toString()));
    }
    return conversations;
  }

  public static Single<List<Conversation>> separateMessagesIntoConversationsAsync(
      List<Message> messages) {
    return Single.fromCallable(new Callable<List<Conversation>>() {
      @Override
      public List<Conversation> call() throws Exception {
        List<String> parentNameList = new LinkedList<>();
        List<Conversation> conversations = new ArrayList<>();

        for (Message message : messages) {
          String parentName = message.getParentMessageName();
          if (parentNameList.contains(parentName)) {
            int conversationIndex = parentNameList.indexOf(parentName);
            Conversation oldConversation = conversations.get(conversationIndex);
            oldConversation.addMessage(message);
            conversations.set(conversationIndex, oldConversation);
          } else {
            parentNameList.add(parentName);
            conversations.add(new Conversation(parentName, message));
          }
        }
        return conversations;
      }
    });
  }
}
