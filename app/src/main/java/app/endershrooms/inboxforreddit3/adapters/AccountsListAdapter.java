package app.endershrooms.inboxforreddit3.adapters;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import app.endershrooms.inboxforreddit3.R;
import app.endershrooms.inboxforreddit3.activities.AddNewAccountActivity;
import app.endershrooms.inboxforreddit3.adapters.AccountsListAdapter.ViewHolder;
import app.endershrooms.inboxforreddit3.models.RedditAccount;
import com.jakewharton.rxbinding2.view.RxView;
import java.util.List;

/**
 * Created by Travis on 3/22/2018.
 */

public class AccountsListAdapter extends RecyclerView.Adapter<ViewHolder> {

  List<RedditAccount> accounts;

  public AccountsListAdapter(List<RedditAccount> accounts) {
    this.accounts = accounts;
  }

  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.account_rv_card, parent, false));
  }

  @Override
  public void onBindViewHolder(ViewHolder holder, int position) {
    if (holder.getAdapterPosition() == accounts.size()) {
      holder.userNameTv.setText("Add account");
      holder.removeBtn.setVisibility(View.GONE);
      holder.parentView.setOnClickListener(view -> {
        Intent i = new Intent(view.getContext(), AddNewAccountActivity.class);
        view.getContext().startActivity(i);
      });
    } else {
      RedditAccount thisAcc = accounts.get(holder.getAdapterPosition());
      holder.userNameTv.setText(thisAcc.getUsername());
      holder.removeBtn.setVisibility(View.VISIBLE);
      RxView.clicks(holder.removeBtn)
          .subscribe(avoid -> {
            //TODO: Handle removing Account
            Log.v("remove account", thisAcc.getUsername());
          });
    }
  }

  @Override
  public int getItemCount() {
    return accounts.size() + 1;
  }

  public void addAccounts(List<RedditAccount> accounts) {
    int initSize = this.accounts.size();
    this.accounts.addAll(accounts);
    notifyItemRangeInserted(initSize, this.accounts.size());
  }

  public void addAccount(RedditAccount account) {
    accounts.add(account);
    notifyDataSetChanged();
  }

  public void updateAccounts(List<RedditAccount> accounts) {
    this.accounts.clear();
    this.accounts.addAll(accounts);
    notifyDataSetChanged();
  }

  class ViewHolder extends RecyclerView.ViewHolder {

    View parentView;
    TextView userNameTv;
    ImageButton removeBtn;

    public ViewHolder(View itemView) {
      super(itemView);

      parentView = itemView.findViewById(R.id.account_card_parentview);
      userNameTv = itemView.findViewById(R.id.account_username);
      removeBtn = itemView.findViewById(R.id.account_removebtn);
    }

  }

}
