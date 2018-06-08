package app.endershrooms.inboxforreddit3.fragments;

import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.View.OnClickListener;
import app.endershrooms.inboxforreddit3.R;

/**
 * Created by Travis on 4/26/2018.
 */

public abstract class BaseFragment extends Fragment {

  @Override
  public void onDestroy() {
    super.onDestroy();
  }

  public void setToolbarBackButton(Toolbar toolbar) {
    setToolbarBackButton(toolbar, null);
  }

  public void setToolbarBackButton(Toolbar toolbar, View.OnClickListener onClickListener) {
    toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_action_back));
    toolbar.setNavigationOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        if (onClickListener != null) {
          onClickListener.onClick(v);
        } else {
          if (getActivity() != null) {
            getActivity().onBackPressed();
          }
        }
      }
    });
  }
}
