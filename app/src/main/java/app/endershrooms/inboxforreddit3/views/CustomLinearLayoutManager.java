package app.endershrooms.inboxforreddit3.views;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.util.AttributeSet;

public class CustomLinearLayoutManager extends LinearLayoutManager {
  private boolean scrollEnabled;

  public CustomLinearLayoutManager(Context context) {
    super(context);
    this.scrollEnabled = true;
  }

  public CustomLinearLayoutManager(Context context, boolean scrollingEnabled) {
    super(context);
    this.scrollEnabled = scrollingEnabled;
  }

  public CustomLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
    super(context, orientation, reverseLayout);
  }

  public CustomLinearLayoutManager(Context context, AttributeSet attrs, int defStyleAttr,
      int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
  }

  public void setScrollEnabled(boolean scrollEnabled) {
    this.scrollEnabled = scrollEnabled;
  }

  @Override
  public boolean canScrollVertically() {
    return scrollEnabled;
  }
}
