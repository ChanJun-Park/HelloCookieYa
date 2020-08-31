package edu.inha.hellocookieya;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import edu.inha.hellocookieya.databinding.FragmentVideoEmptyBinding;
import edu.inha.hellocookieya.video.VideoListFragmentCallback;
import com.google.android.youtube.player.YouTubeIntents;

import org.jetbrains.annotations.NotNull;

import timber.log.Timber;

import static edu.inha.hellocookieya.RequestCodeConstant.REQUEST_ADD_VIDEO;


public class VideoEmptyFragment extends Fragment {

    private FragmentVideoEmptyBinding binding;
    private VideoListFragmentCallback callback;

    public VideoEmptyFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);

        callback = (VideoListFragmentCallback) getActivity();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentVideoEmptyBinding.inflate(inflater, container, false);
        ViewGroup rootView = binding.getRoot();

        binding.plusVideoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (YouTubeIntents.isYouTubeInstalled(getContext())) {
                    Intent youtubeLaunchIntent = new Intent(Intent.ACTION_VIEW);
                    youtubeLaunchIntent.setData(Uri.parse("https://www.youtube.com"));
                    youtubeLaunchIntent.setPackage("com.google.android.youtube");
                    startActivity(youtubeLaunchIntent);
                }
            }
        });

        return rootView;
    }

    public void requestAddVideo(String sharedText) {
        Intent intent = new Intent(getContext(), AddVideoActivity.class);
        intent.putExtra("sharedText", sharedText);
        startActivityForResult(intent, REQUEST_ADD_VIDEO);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Timber.d("onActivityResult() 호출됨");

        if (requestCode == REQUEST_ADD_VIDEO) {
            if (resultCode == Activity.RESULT_OK) {
                if (callback != null) {
                    callback.onVideoAdded();
                }
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
