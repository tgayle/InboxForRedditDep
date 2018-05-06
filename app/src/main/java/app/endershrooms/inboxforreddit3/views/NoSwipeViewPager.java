package app.endershrooms.inboxforreddit3.views;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by Travis on 1/19/2018.
 */

public class NoSwipeViewPager extends ViewPager {

  private boolean enabled = false;

  public NoSwipeViewPager(Context context) {
    super(context);
  }

  public NoSwipeViewPager(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    if (this.enabled) {
      return super.onTouchEvent(event);
    }
    return false;
  }

  @Override
  public boolean onInterceptTouchEvent(MotionEvent event) {
    if (this.enabled) {
      return super.onInterceptTouchEvent(event);
    }
    return false;
  }

  public void setPagingEnabled(boolean enabled) {
    this.enabled = enabled;
  }

}
