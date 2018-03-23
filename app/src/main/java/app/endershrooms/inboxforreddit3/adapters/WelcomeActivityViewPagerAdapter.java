package app.endershrooms.inboxforreddit3.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import app.endershrooms.inboxforreddit3.fragments.LoginFragment;
import app.endershrooms.inboxforreddit3.fragments.WelcomeActivityFragment;

/**
 * Created by Travis on 1/19/2018.
 */

public class WelcomeActivityViewPagerAdapter extends FragmentStatePagerAdapter {

  public static final int WELCOME = 0;
  public static final int LOGIN = 1;
  public static final int LOADING = 2;

  public WelcomeActivityViewPagerAdapter(FragmentManager fm) {
    super(fm);
  }

  @Override
  public int getCount() {
    return 3; //welcome, login, loading
  }

  @Override
  public Fragment getItem(int position) {
    switch (position) {
      case WELCOME:
        return WelcomeActivityFragment
            .newInstance(WelcomeActivityFragment.FragmentProgress.WELCOME);
      case LOGIN:
        return LoginFragment.newInstance();
      case LOADING:
        return WelcomeActivityFragment
            .newInstance(WelcomeActivityFragment.FragmentProgress.LOADING);

    }

    return null;
  }

  @Override
  public CharSequence getPageTitle(int position) {
    return super.getPageTitle(position);
  }
}
