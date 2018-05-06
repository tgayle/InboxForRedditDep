package app.endershrooms.inboxforreddit3.account;

/**
 * Created by Travis on 4/24/2018.
 */

public class AccountManager {

  private static final AccountManager ourInstance = new AccountManager();

  public static AccountManager getInstance() {
    return ourInstance;
  }

  private AccountManager() {
  }
}
