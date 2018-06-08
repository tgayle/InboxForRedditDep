package app.endershrooms.inboxforreddit3.fragments;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import app.endershrooms.inboxforreddit3.R;
import app.endershrooms.inboxforreddit3.viewmodels.login.EntryLoginActivityViewModel;


public class WelcomeActivityFragment extends Fragment {


  public enum FragmentLoadingProgress {
    WELCOME,
    LOGIN,
    LOADING
  }

  private FragmentLoadingProgress progress;

  public WelcomeActivityFragment() {
    // Required empty public constructor
  }

  public static WelcomeActivityFragment newInstance(FragmentLoadingProgress progress) {
    WelcomeActivityFragment fragment = new WelcomeActivityFragment();
    Bundle args = new Bundle();
    args.putSerializable("progress", progress);
    fragment.setArguments(args);

    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (getArguments() != null) {
      this.progress = (FragmentLoadingProgress) getArguments().getSerializable("progress");
    }
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    EntryLoginActivityViewModel viewModel = ViewModelProviders.of(getActivity()).get(EntryLoginActivityViewModel.class);

    if (progress == FragmentLoadingProgress.WELCOME) {
      Button welcomeLoginBtn = (Button) getView().findViewById(R.id.login_btn);
      welcomeLoginBtn.setOnClickListener(
          view -> {
            viewModel.changeLoginProgress(FragmentLoadingProgress.LOGIN);
          });
    } else if (progress == FragmentLoadingProgress.LOADING) {
      TextView loadingProgressText = (TextView) getView().findViewById(R.id.progress_tv);
      loadingProgressText.setVisibility(View.VISIBLE);

      viewModel.getLoginProgressText().observe(this, newText -> {
        loadingProgressText.setText(newText);
      });
    }

  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View v = null;

    switch (progress) {
      case WELCOME:
        v = inflater.inflate(R.layout.fragment_welcome, container, false);
        Log.v("Fragment", "Started welcome fragment!");
        break;
      case LOADING:
        v = inflater.inflate(R.layout.fragment_welcome_loading, container, false);
        Log.v("Fragment", "Started loading fragment!");
        break;
      default:
        throw new RuntimeException("Tried to create fragment with an unknown progress.");
    }

    return v;
  }

}
