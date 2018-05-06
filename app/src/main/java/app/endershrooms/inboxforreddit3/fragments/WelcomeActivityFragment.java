package app.endershrooms.inboxforreddit3.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import app.endershrooms.inboxforreddit3.R;
import app.endershrooms.inboxforreddit3.Singleton;
import app.endershrooms.inboxforreddit3.activities.EntryLoginActivity;
import app.endershrooms.inboxforreddit3.activities.MessagesActivity;
import app.endershrooms.inboxforreddit3.models.RedditAccount;
import com.jakewharton.rxbinding2.view.RxView;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


public class WelcomeActivityFragment extends Fragment implements EntryLoginActivity.LoginUpdateListener {


  public enum FragmentProgress {
    WELCOME,
    LOADING

  }

  private FragmentProgress progress;
  private Observer<String> loginProgressObserver;

  public WelcomeActivityFragment() {
    // Required empty public constructor
  }

  public static WelcomeActivityFragment newInstance(FragmentProgress progress) {
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
      this.progress = (FragmentProgress) getArguments().getSerializable("progress");
    }

    loginProgressObserver = new Observer<String>() {
      @Override
      public void onSubscribe(Disposable d) {

      }

      @Override
      public void onNext(String s) {
        if (progress == FragmentProgress.LOADING && getView() != null) {
          TextView tv = (TextView) getView().findViewById(R.id.progress_tv);
          tv.setText(s);
        }

      }

      @Override
      public void onError(Throwable e) {

      }

      @Override
      public void onComplete() {

      }
    };
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View v = null;

    switch (progress) {
      case WELCOME:
        v = inflater.inflate(R.layout.fragment_welcome, container, false);

        Button loginBtn = (Button) v.findViewById(R.id.login_btn);

        RxView.clicks(loginBtn)
            .subscribe(aVoid -> {
              ((EntryLoginActivity) getActivity()).startLogin();
            });

        Log.v("Fragment", "Started welcome fragment!");
        break;
      case LOADING:
        v = inflater.inflate(R.layout.fragment_welcome_loading, container, false);
        TextView progressText = (TextView) v.findViewById(R.id.progress_tv);
        progressText.setVisibility(View.VISIBLE);
        Log.v("Fragment", "Started loading fragment!");

        break;
      default:
        throw new RuntimeException("Tried to create fragment with an unknown progress.");
    }

    return v;
  }

  @Override
  public void updateLoadingText(final String text) {
    if (progress == FragmentProgress.LOADING && getView() != null) {

      Observable.just(text)
          .subscribeOn(AndroidSchedulers.mainThread())
          .subscribe(o -> {
            TextView tv = (TextView) getView().findViewById(R.id.progress_tv);
            tv.setText(o);
          });

//      getActivity().runOnUiThread(new Runnable() {
//        @Override
//        public void run() {
//          TextView tv = (TextView) getView().findViewById(R.id.progress_tv);
//          tv.setText(text);
//        }
//      });

    }
  }

  @Override
  public void onCompleteLogin(final RedditAccount account) {

    Observable.just(account)
        .subscribeOn(Schedulers.io())
        .subscribe(acc -> {
          Singleton.get().getDb().accounts().addAccount(account);
        });
//    TODO: Move these two into one observable.
    Observable.just(account)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(acc -> {
          Intent i = new Intent(getActivity(), MessagesActivity.class);
          i.putExtra("account", account);
          startActivity(i);
          getActivity().finish();
        });
  }
  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    ((EntryLoginActivity)getActivity()).setFragmentLoginListener(this);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    ((EntryLoginActivity) getActivity()).unregisterDataUpdateListener(this);
  }

  @Override
  public void onDetach() {
    super.onDetach();
  }

  public interface OnFragmentInteractionListener {

    void onFragmentInteraction(Uri uri);
  }

}
