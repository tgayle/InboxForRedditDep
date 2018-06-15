package app.endershrooms.inboxforreddit3.database;

public class LocalMessageState {

  private boolean inInbox;
  private boolean inDeleted;
  private boolean isHidden;
  private boolean isPending;

  public LocalMessageState(boolean inInbox, boolean inDeleted, boolean isHidden, boolean isPending) {
    this.inInbox = inInbox;
    this.inDeleted = inDeleted;
    this.isHidden = isHidden;
    this.isPending = isPending;
  }

  public void removeAllStatesExceptOne(LocalMessageStates stateToBeLeft) {
    switch (stateToBeLeft) {
      case INBOX:
        inDeleted = false;
        isHidden = false;
        isPending = false;
        inInbox = true;
        break;
      case DELETED:
        inInbox = false;
        isHidden = false;
        isPending = false;
        inDeleted = true;
        break;
      case HIDDEN:
        inInbox = false;
        inDeleted = false;
        isPending = false;
        isHidden = true;
        break;
      case PENDING:
        inInbox = false;
        inDeleted = false;
        isHidden = false;
        isPending = true;
        break;
    }
  }

  public boolean isInInbox() {
    return inInbox;
  }

  public void setInInbox(boolean inInbox) {
    this.inInbox = inInbox;
  }

  public boolean isInDeleted() {
    return inDeleted;
  }

  public void setInDeleted(boolean inDeleted) {
    this.inDeleted = inDeleted;
  }

  public boolean isHidden() {
    return isHidden;
  }

  public void setHidden(boolean hidden) {
    isHidden = hidden;
  }

  public boolean isPending() {
    return isPending;
  }

  public void setPending(boolean pending) {
    isPending = pending;
  }
}
