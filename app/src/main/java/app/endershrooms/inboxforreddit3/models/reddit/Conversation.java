package app.endershrooms.inboxforreddit3.models.reddit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

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

  /**
   * Convert a list of messages into a list of conversations by separating them by parent name.
   * With how this is structured, the array expects that the first message in the list is the
   * chronologically oldest message, so the last message in the list should be the newest message.
   * @param messages A List of messages, with the oldest messages being at index 0
   * @return A List of Conversations with the oldest conversation being at index 0, and each list of messages in a conversation will have the oldest message there at index 0.
   */
  public static List<Conversation> formConversations(List<Message> messages) {
    LinkedHashMap<String, Conversation> conversations = new LinkedHashMap<>();

    //Loop through the messages backwards so that messages come in the proper order.
    for (int i = messages.size() - 1; i >= 0; i--) {
      Message message = messages.get(i);
      String parent = message.getParentMessageName();

      if (conversations.containsKey(parent)) {
        conversations.get(parent).addMessage(message);
      } else {
        conversations.put(parent, new Conversation(parent, message));
      }
    }
    for (Conversation conversation : conversations.values()) {
      Collections.reverse(conversation.messages);
    }
    ArrayList<Conversation> returnedList = new ArrayList<>(conversations.values());
    Collections.reverse(returnedList);
    return returnedList;
  }

}
