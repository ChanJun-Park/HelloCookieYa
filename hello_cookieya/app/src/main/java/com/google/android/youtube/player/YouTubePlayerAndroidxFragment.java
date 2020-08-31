package com.google.android.youtube.player;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.youtube.player.YouTubePlayerView.b;
import com.google.android.youtube.player.internal.ab;

public class YouTubePlayerAndroidxFragment extends Fragment implements YouTubePlayer.Provider {

    private static final String KEY_PLAYER_VIEW_STATE = "YouTubePlayerAndroidxFragment.KEY_PLAYER_VIEW_STATE";
    private Bundle bundle = null;
    private YouTubePlayerView youTubePlayerView = null;
    private String developerKey;
    private YouTubePlayer.OnInitializedListener onInitializedListener;

    private OnYouTubePlayerFragmentStateChanged activity;

    @Override
    public void initialize(String developerKey, YouTubePlayer.OnInitializedListener onInitializedListener) {
        this.developerKey = ab.a(developerKey, "Developer key cannot be null or empty");
        this.onInitializedListener = onInitializedListener;
        initYoutubePlayerView();
    }

    private void initYoutubePlayerView() {
        if (youTubePlayerView != null && onInitializedListener != null) {
            youTubePlayerView.a(false);
            youTubePlayerView.a(getActivity(), this, developerKey, onInitializedListener, bundle);
            bundle = null;
            onInitializedListener = null;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            bundle = savedInstanceState.getBundle(KEY_PLAYER_VIEW_STATE);
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (OnYouTubePlayerFragmentStateChanged) getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        youTubePlayerView = new YouTubePlayerView(getActivity(), null, 0, new OnYoutubeInitializedListener());
        initYoutubePlayerView();
        return youTubePlayerView;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (youTubePlayerView != null)
            youTubePlayerView.a();
    }


    @Override
    public void onResume() {
        super.onResume();
        if (youTubePlayerView != null)
            youTubePlayerView.b();
        activity.onFragmentResume();
    }

    @Override
    public void onPause() {
        if (youTubePlayerView != null)
            youTubePlayerView.c();
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Bundle bundle = this.bundle;
        if (youTubePlayerView != null) {
            bundle = youTubePlayerView.e();
        }
        outState.putBundle(KEY_PLAYER_VIEW_STATE, bundle);
    }

    @Override
    public void onStop() {
        if (youTubePlayerView != null) {
            youTubePlayerView.d();
        }
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        if (youTubePlayerView != null) {
            Activity activity = getActivity();
            if (activity != null) {
                youTubePlayerView.c(activity.isFinishing());
            } else {
                youTubePlayerView.c(false);
            }
            youTubePlayerView = null;
        }
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        if (youTubePlayerView != null) {
            Activity activity = getActivity();
            if (activity != null) {
                youTubePlayerView.b(activity.isFinishing());
            } else {
                youTubePlayerView.b(true);
            }
        }
        super.onDestroy();
    }

    public class OnYoutubeInitializedListener implements b {

        @Override
        public void a(YouTubePlayerView youTubePlayerView, String s, YouTubePlayer.OnInitializedListener onInitializedListener) {
            initialize(s, onInitializedListener);
        }

        @Override
        public void a(YouTubePlayerView youTubePlayerView) {

        }
    }

    public interface OnYouTubePlayerFragmentStateChanged {
        void onFragmentResume();
    }
}