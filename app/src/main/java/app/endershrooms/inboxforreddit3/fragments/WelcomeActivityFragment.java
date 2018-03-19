package app.endershrooms.inboxforreddit3.fragments;

import android.content.Context;
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
import app.endershrooms.inboxforreddit3.activities.MainActivity;


public class WelcomeActivityFragment extends Fragment implements MainActivity.LoginUpdateListener {


    public enum FragmentProgress {
        WELCOME,
        LOADING

    }

    private FragmentProgress progress;

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = null;

        switch (progress) {
            case WELCOME:
                v =  inflater.inflate(R.layout.fragment_welcome, container, false);

                Button loginBtn = (Button) v.findViewById(R.id.login_btn);

                loginBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //Intent intent = new Intent(getContext(), LoginWebViewActivity.class);
                        //getActivity().startActivityForResult(intent, 111);

                        ((MainActivity) getActivity()).startLogin();
                    }
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
    public void updateLoadingText(String text) {
        if (progress == FragmentProgress.LOADING && getView() != null) {
            TextView tv = (TextView) getView().findViewById(R.id.progress_tv);
            tv.setText(text);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ((MainActivity) getActivity()).registerDataUpdateListener(this);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ((MainActivity) getActivity()).unregisterDataUpdateListener(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

}
