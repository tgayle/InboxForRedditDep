package app.endershrooms.inboxforreddit3.adapters;

import android.arch.paging.PagedListAdapter;
import android.content.Intent;
import android.support.v7.util.DiffUtil;
import android.support.v7.util.DiffUtil.ItemCallback;
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
import app.endershrooms.inboxforreddit3.interfaces.OnAccountListInteraction;
import app.endershrooms.inboxforreddit3.models.reddit.RedditAccount;

/**
 * Created by Travis on 3/22/2018.
 */

public class AccountsListAdapter extends PagedListAdapter<RedditAccount, ViewHolder> {

  OnAccountListInteraction accountListInteraction;
  public AccountsListAdapter(OnAccountListInteraction onAccountRemovedListener) {
    super(ACCOUNT_DIFFERENCE_CALLBACK);
    this.accountListInteraction = onAccountRemovedListener;
  }

  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    return new ViewHolder(LayoutInflater.from(parent.getContext())
        .inflate(R.layout.account_rv_card, parent, false));
  }

  @Override
  public void onBindViewHolder(ViewHolder holder, int position) {
    if (holder.getAdapterPosition() != getCurrentList().size()) {
      RedditAccount acc = getItem(position);
      if (acc != null) {
        holder.bind(acc);
      } else {
        holder.clear();
      }
    } else {
      holder.clear();
      holder.getAddAccountView();
    }
  }

  private static final DiffUtil.ItemCallback<RedditAccount> ACCOUNT_DIFFERENCE_CALLBACK =
      new ItemCallback<RedditAccount>() {
        @Override
        public boolean areItemsTheSame(RedditAccount oldItem, RedditAccount newItem) {
          return oldItem.getUsername().equals(newItem.getUsername());
        }

        @Override
        public boolean areContentsTheSame(RedditAccount oldItem, RedditAccount newItem) {
          return oldItem.equals(newItem);
        }
      };

  @Override
  public int getItemCount() {
    if (getCurrentList() == null) return 0;
    return getCurrentList().size() + 1;
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

    public void getAddAccountView() {
      this.userNameTv.setText("Add account");
      this.removeBtn.setVisibility(View.GONE);
      this.parentView.setOnClickListener(null);
      this.parentView.setOnClickListener(view -> {
        Intent i = new Intent(view.getContext(), AddNewAccountActivity.class);
        view.getContext().startActivity(i);
      });
    }

    public void bind(RedditAccount acc) {
      this.userNameTv.setText(acc.getUsername());
      this.removeBtn.setVisibility(View.VISIBLE);

      this.parentView.setOnClickListener(view -> {
        accountListInteraction.onAccountSelected(acc);
        Log.d("AccountsListAdapter", "Account selected " + acc.getUsername());
      });

      this.removeBtn.setOnClickListener(view -> {
        accountListInteraction.onAccountRemoved(acc);
        Log.v("AccountsListAdapter", acc.getUsername());
      });
    }

    public void clear() {
      this.userNameTv.setText("");
      this.removeBtn.setVisibility(View.VISIBLE);
      this.parentView.setOnClickListener(null);
      this.removeBtn.setOnClickListener(null);
    }
  }

}
