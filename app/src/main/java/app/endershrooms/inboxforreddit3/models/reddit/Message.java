package app.endershrooms.inboxforreddit3.models.reddit;

import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import app.endershrooms.inboxforreddit3.database.LocalMessageState;

/**
 * Created by Travis on 1/22/2018.
 */
@Entity(tableName = "messages")
public class Message {

  @PrimaryKey
  @NonNull
  private String messageName;
  private String messageOwner;
  private String parentMessageName;
  private String author;
  private String destination;
  private String subject;
  private String messageBody;
  private long timestamp;
  private boolean isNew;

  @Embedded
  private LocalMessageState messageState;


  public Message(String messageOwner, @NonNull String messageName, String parentMessageName,
      String author, String destination, String subject, String messageBody,
      long timestamp, boolean isNew, @Nullable LocalMessageState messageState) {
    this.messageOwner = messageOwner;
    this.messageName = messageName;
    this.parentMessageName = parentMessageName;
    this.author = author;
    this.destination = destination;
    this.subject = subject;
    this.messageBody = messageBody;
    this.timestamp = timestamp;
    this.isNew = isNew;
    this.messageState = messageState;
    if (messageState == null) {
      this.messageState = new LocalMessageState(true, false, false, false);
    }
  }

  public Message(RedditAccount messageOwner, @NonNull String messageName, String parentMessageName,
      String author, String destination, String subject, String messageBody,
      long timestamp, boolean isNew, LocalMessageState messageState) {
    this(messageOwner.getUsername(), messageName, parentMessageName, author,
        destination, subject, messageBody, timestamp, isNew, messageState);
  }

  @NonNull
  public String getMessageName() {
    return messageName;
  }

  public String getParentMessageName() {
    return parentMessageName;
  }

  public String getAuthor() {
    return author;
  }

  public String getDestination() {
    return destination;
  }

  public String getSubject() {
    return subject;
  }

  public String getMessageBody() {
    return messageBody;
  }

  public Long getTimestamp() {
    return timestamp;
  }

  public boolean getNew() {
    return isNew;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  public void setDestination(String destination) {
    this.destination = destination;
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }

  public void setMessageBody(String message_body) {
    this.messageBody = message_body;
  }

  public void setTimestamp(Long timestamp) {
    this.timestamp = timestamp;
  }

  public void setMessageName(@NonNull String messageName) {
    this.messageName = messageName;
  }

  public void setParentMessageName(String parentMessageName) {
    this.parentMessageName = parentMessageName;
  }

  public String getMessageOwner() {
    return messageOwner;
  }

  public void setMessageOwner(String messageOwner) {
    this.messageOwner = messageOwner;
  }

  public void setNew(boolean is_new) {
    this.isNew = is_new;
  }

  public LocalMessageState getMessageState() {
    return messageState;
  }

  public void setMessageState(LocalMessageState messageState) {
    this.messageState = messageState;
  }

  public String getCorrespondent() {
    if (messageOwner.equals(author)) {
      return destination;
    }
    return author;
  }

  public boolean didCurrentUserSendMessage() {
    return this.getMessageOwner().equals(this.getAuthor());
  }

  @Override
  public int hashCode() {
    return messageName.hashCode() + parentMessageName.hashCode() + messageOwner.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj instanceof Message) {
      Message otherMsg = (Message) obj;
      return this.getMessageName().equals(otherMsg.getMessageName())          &&
          this.getAuthor().equals(otherMsg.getAuthor())                       &&
          this.getDestination().equals(otherMsg.getDestination())             &&
          this.getParentMessageName().equals(otherMsg.getParentMessageName()) &&
          this.isNew == otherMsg.isNew                                        &&
          this.getTimestamp().equals(otherMsg.getTimestamp())                 &&
          this.getMessageOwner().equals(otherMsg.getMessageOwner());
    }
    return false;
  }
}
