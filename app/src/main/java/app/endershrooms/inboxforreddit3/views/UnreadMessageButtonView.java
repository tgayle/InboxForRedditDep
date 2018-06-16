package app.endershrooms.inboxforreddit3.views;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.TextView;
import app.endershrooms.inboxforreddit3.R;

public class UnreadMessageButtonView extends FrameLayout {

  private CardView parentCardView;
  private TextView unreadMessageNumTextView;

  public UnreadMessageButtonView(Context context) {
    this(context, null);
  }

  public UnreadMessageButtonView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  private void init() {
    inflate(getContext(), R.layout.unread_messages_toolbar_view, this);
    parentCardView = findViewById(R.id.menu_unread_message_parent_cardview);
    unreadMessageNumTextView = findViewById(R.id.menu_unread_message_btn);
    hide(); //Hidden by default
  }

  public void setUnreadMessages(Integer num) {
    if (num == null || num == 0) {
      hide();
      return;
    }
    unreadMessageNumTextView.setText(String.valueOf(num));
    show();
  }

  public void hide() {
    parentCardView.setClickable(false);
    parentCardView.setEnabled(false);
    animate().setDuration(75).alpha(0f).withEndAction(() -> parentCardView.setVisibility(INVISIBLE)).start();
  }

  public void show() {
    parentCardView.setClickable(true);
    parentCardView.setEnabled(true);
    animate().setDuration(100).alpha(1f).withStartAction(() -> parentCardView.setVisibility(VISIBLE)).start();
  }

  @Override
  public void setOnClickListener(@Nullable OnClickListener l) {
    parentCardView.setOnClickListener(l);
  }
}
