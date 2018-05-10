package app.endershrooms.inboxforreddit3.activities;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import app.endershrooms.inboxforreddit3.R;
import app.endershrooms.inboxforreddit3.fragments.LoginFragment;
import app.endershrooms.inboxforreddit3.viewmodels.AddNewAccountActivityViewModel;

public class AddNewAccountActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_add_new_account);
    getSupportActionBar().hide();
    getSupportFragmentManager()
        .beginTransaction()
        .add(R.id.fragment_holder, LoginFragment.newInstance())
        .commit();

    AddNewAccountActivityViewModel viewModel = ViewModelProviders.of(this).get(AddNewAccountActivityViewModel.class);
    viewModel.getAddedAccount().observe(this, account -> {
      if (account != null) {
        Log.d("AddNewActivity", "account was " + account.getUsername());
        finish();
      }
    });
  }

}
