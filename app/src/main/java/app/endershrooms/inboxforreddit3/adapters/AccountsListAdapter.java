package app.endershrooms.inboxforreddit3.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import app.endershrooms.inboxforreddit3.adapters.AccountsListAdapter.ViewHolder;

/**
 * Created by Travis on 3/22/2018.
 */

public class AccountsListAdapter extends RecyclerView.Adapter<ViewHolder> {

  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    return null;
  }

  @Override
  public void onBindViewHolder(ViewHolder holder, int position) {

  }

  @Override
  public int getItemCount() {
    return 0;
  }

  class ViewHolder extends RecyclerView.ViewHolder {



    public ViewHolder(View itemView) {
      super(itemView);
    }
  }

}
