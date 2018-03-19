package app.endershrooms.inboxforreddit3.models;

/**
 * Created by Travis on 1/22/2018.
 */

public class Message {

    String  message_name;
    String  parent_message_name;
    String  author;
    String  destination;
    String  subject;
    String  message_body;
    Long    timestamp;
    boolean is_new;

    public Message(String message_name, String parent_message_name, String author, String destination, String subject, String message_body, Long timestamp, boolean is_new) {
        this.message_name        = message_name;
        this.parent_message_name = parent_message_name;
        this.author              = author;
        this.destination         = destination;
        this.subject             = subject;
        this.message_body        = message_body;
        this.timestamp           = timestamp;
        this.is_new              = is_new;
    }

    public String getMessageName() {
        return message_name;
    }

    public String getParentMessageName() {
        return parent_message_name;
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
        return message_body;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public boolean getNew() {
        return is_new;
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

    public void setMessage_body(String message_body) {
        this.message_body = message_body;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public void setNew(boolean is_new) {
        this.is_new = is_new;
    }
}