package app.endershrooms.inboxforreddit3.views;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by Travis on 3/23/2018.
 */

public class RecyclerViewVerticalSpace extends RecyclerView.ItemDecoration {
  private final int verticalSpaceHeight;

  public RecyclerViewVerticalSpace(int verticalSpaceHeight) {
    this.verticalSpaceHeight = verticalSpaceHeight;
  }

  @Override
  public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
      RecyclerView.State state) {
    outRect.bottom = verticalSpaceHeight;
  }
}
