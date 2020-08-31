package edu.inha.hellocookieya;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import edu.inha.hellocookieya.databinding.FragmentVideoListBinding;
import edu.inha.hellocookieya.db.DBManager;
import edu.inha.hellocookieya.playlist.PlaylistItem;
import edu.inha.hellocookieya.speech.command.Command;
import edu.inha.hellocookieya.video.OnVideoPreviewItemClickListener;
import edu.inha.hellocookieya.video.VideoItem;
import edu.inha.hellocookieya.video.VideoListFragmentCallback;
import edu.inha.hellocookieya.video.VideoPreviewAdapter;
import com.google.android.youtube.player.YouTubeIntents;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import timber.log.Timber;

import static android.app.Activity.RESULT_OK;
import static androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE;
import static edu.inha.hellocookieya.RequestCodeConstant.REQUEST_ADD_VIDEO;
import static edu.inha.hellocookieya.RequestCodeConstant.REQUEST_PLAY_VIDEO;

public class VideoListFragment extends Fragment {


    private FragmentVideoListBinding binding;

    private VideoListFragmentCallback videoListFragmentCallback;

    private VideoPreviewAdapter videoPreviewAdapter;
    private CustomScrollLinearLayoutManager layoutManager;
    private boolean isAutoScrolling = false;
    private boolean isScrollingUp = false;

    private RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(@NotNull RecyclerView recyclerView, int newState) {
            if (newState == SCROLL_STATE_IDLE) {
                if (isAutoScrolling) {
                    if (isScrollingUp) {
                        isScrollingUp = false;
                        scrollDown();
                    } else {
                        isScrollingUp = true;
                        scrollUp();
                    }
                } else {
                    recyclerView.removeOnScrollListener(this);
                }
            }
        }
    };

    private VideoItem receivedItemFromMainActivity = null;

    private PlaylistItem curPlaylistItem = null;

    private VideoItem playedVideoItem = null;
    protected boolean isCommandExist = false;
    protected Command deliveredCommand;

    public VideoListFragment() {
        // Required empty public constructor
    }

    public void setReceivedItemFromMainActivity(VideoItem receivedItemFromMainActivity) {
        this.receivedItemFromMainActivity = receivedItemFromMainActivity;
    }

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        Timber.d("onAttach 호출됨");

        videoListFragmentCallback = (VideoListFragmentCallback) getActivity();
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentVideoListBinding.inflate(inflater, container, false);
        ViewGroup rootView = binding.getRoot();
        Timber.d("onCreateView 호출됨");

        layoutManager = new CustomScrollLinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        binding.videoPreviewRecyclerView.setLayoutManager(layoutManager);

        videoPreviewAdapter = new VideoPreviewAdapter(this);

        getPlaylistItems();

        setListener();

        binding.videoPreviewRecyclerView.setAdapter(videoPreviewAdapter);

        if (videoListFragmentCallback != null)
            videoListFragmentCallback.onFragmentCreateView();
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        Timber.d("onResume 호출됨");

        if (receivedItemFromMainActivity != null) {
            if (receivedItemFromMainActivity.getPlaylistId() == curPlaylistItem.get_id()) {
                videoPreviewAdapter.addItem(receivedItemFromMainActivity);
                videoPreviewAdapter.notifyDataSetChanged();
                videoListFragmentCallback.resetToolbarText();
            }
            receivedItemFromMainActivity = null;
        }

        processDeliveredCommand();
    }

    public PlaylistItem getCurPlaylistItem() {
        return curPlaylistItem;
    }

    public void setCurPlaylistItem(PlaylistItem curPlaylistItem) {
        this.curPlaylistItem = curPlaylistItem;
    }

    public void resetPlaylist(PlaylistItem playlistItem) {
        curPlaylistItem = playlistItem;
        ArrayList<VideoItem> items = DBManager.getInstance(getContext()).getVideoList(curPlaylistItem.get_id());
        if (items != null) {
            if (items.size() != 0) {
                Timber.d("플레이 리스트 아이템 갱신됨");
                videoPreviewAdapter.setItems(items);
                videoPreviewAdapter.notifyDataSetChanged();
            }
            else {
                videoPreviewAdapter.setItems(null);
                videoListFragmentCallback.onVideoEmpty();
            }
        }
    }

    private void getPlaylistItems() {
        if (curPlaylistItem == null) {
            Timber.e("먼저 플레이 리스트를 전달해야합니다.");
            videoListFragmentCallback.onVideoEmpty();
        } else {
            ArrayList<VideoItem> items = DBManager.getInstance(getContext()).getVideoList(curPlaylistItem.get_id());
            if (items != null) {
                if (items.size() != 0) {
                    videoPreviewAdapter.setItems(items);
                }
                else {
                    videoPreviewAdapter.setItems(null);
                    videoListFragmentCallback.onVideoEmpty();
                }
            }
        }
    }

    private void setListener() {
        videoPreviewAdapter.setPreviewItemClickListener(new OnVideoPreviewItemClickListener() {
            @Override
            public void onItemClicked(int position) {
                VideoItem item = videoPreviewAdapter.getItem(position);
                Toast.makeText(getContext(), "아이템 선택됨 : " + item.getTitle(), Toast.LENGTH_LONG).show();

                Intent intent = new Intent(getContext(), PlayVideoActivity.class);
                intent.putExtra("videoItem", item);
                startActivityForResult(intent, REQUEST_PLAY_VIDEO);
            }

            @Override
            public void onItemDeleteClicked(final int position) {
                final VideoItem item = videoPreviewAdapter.getItem(position);

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("안내")
                        .setMessage("이 영상 링크를 제거하시겠습니까?")
                        .setPositiveButton("예", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                DBManager.getInstance(getContext()).deleteVideoLink(item.get_id());
                                videoPreviewAdapter.deleteItem(position);
                                videoPreviewAdapter.notifyDataSetChanged();
                                Toast.makeText(getContext(), "아이템 제거됨 : " + item.getTitle(), Toast.LENGTH_LONG).show();

                                videoListFragmentCallback.onVideoDeleted();

                                if (videoPreviewAdapter.getItemCount() == 0) {
                                    videoListFragmentCallback.onVideoEmpty();
                                }
                            }
                        })
                        .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        binding.plusVideoFloatingButton.setOnClickListener(new View.OnClickListener() {
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
    }

    public int getListItemCount() {
        int count = 0;
        if (videoPreviewAdapter != null) {
            count = videoPreviewAdapter.getItemCount();
        }
        return count;
    }

    public void requestAddVideo(String sharedText) {
        Intent intent = new Intent(getContext(), AddVideoActivity.class);
        intent.putExtra("sharedText", sharedText);
        startActivityForResult(intent, REQUEST_ADD_VIDEO);
    }

    // be sure about that the parameter index delivered here should be started from 1
    public void playNthVideo(int position) {
        VideoItem item = videoPreviewAdapter.getItem(position - 1);

        if (item == null) {
            Toast.makeText(getContext(), "해당하는 번호의 영상이 존재하지 않습니다", Toast.LENGTH_LONG).show();
            return;
        }
        Intent intent = new Intent(getContext(), PlayVideoActivity.class);
        intent.putExtra("videoItem", item);
        startActivityForResult(intent, REQUEST_PLAY_VIDEO);
    }

    private void processDeliveredCommand() {
        if (isCommandExist) {
            if (deliveredCommand == null || playedVideoItem == null) {
                Timber.e("PlayVideoActivity 에서 넘어온 데이터가 널값임");
                return;
            }
            switch (deliveredCommand.getCmdType()) {
                case COMMAND_PLAY_NEXT_VIDEO: {
                    int position = videoPreviewAdapter.getItemPosition(playedVideoItem);
                    position++; // index started from 1
                    position++;  // next video index
                    playNthVideo(position);
                    break;
                }
                case COMMAND_PLAY_PREV_VIDEO: {
                    int position = videoPreviewAdapter.getItemPosition(playedVideoItem);
//                            position++; // index started from 1
//                            position--;  // prev video index
                    playNthVideo(position);
                    break;
                }
            }
        }
        isCommandExist = false;
    }

    public void startAutoScroll() {
        if (!isListScrollable()) {
            return;
        }
        Timber.d("자동 스크롤 시작");
        binding.videoPreviewRecyclerView.removeOnScrollListener(onScrollListener);
        binding.videoPreviewRecyclerView.addOnScrollListener(onScrollListener);
        isAutoScrolling = true;

        if (layoutManager.findLastCompletelyVisibleItemPosition() == videoPreviewAdapter.getItemCount() - 1) {
            isScrollingUp = true;
            scrollUp();
        } else {
            isScrollingUp = false;
            scrollDown();
        }
    }

    private void scrollDown() {
        layoutManager.smoothScrollToPosition(
                binding.videoPreviewRecyclerView,
                new RecyclerView.State(),
                videoPreviewAdapter.getItemCount() - 1);
    }

    private void scrollUp() {
        layoutManager.smoothScrollToPosition(
                binding.videoPreviewRecyclerView,
                new RecyclerView.State(),
                0);
    }

    public void stopAutoScroll() {
        Timber.d("자동 스크롤 중지");
        isAutoScrolling = false;
        int currentPosition = layoutManager.findFirstCompletelyVisibleItemPosition();
        layoutManager.smoothScrollToPosition(
                binding.videoPreviewRecyclerView,
                new RecyclerView.State(),
                currentPosition
        );
    }

    private boolean isListScrollable() {
        return !(layoutManager.findLastCompletelyVisibleItemPosition() == videoPreviewAdapter.getItemCount() - 1)
                || !(layoutManager.findFirstCompletelyVisibleItemPosition() == 0);
    }

    public void deleteNthVideo(int position) {
        VideoItem item = videoPreviewAdapter.getItem(position - 1);

        if (item == null) {
            Toast.makeText(getContext(), "해당하는 번호의 영상이 존재하지 않습니다", Toast.LENGTH_LONG).show();
        } else {
            DBManager.getInstance(getContext())
                    .deleteVideoLink(item.get_id());
            videoPreviewAdapter.deleteItem(position - 1);
            videoPreviewAdapter.notifyDataSetChanged();
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Timber.d("onActivityResult() 호출됨");

        if (requestCode == REQUEST_ADD_VIDEO) {
            if (resultCode == RESULT_OK) {
                if (data != null
                        && data.hasExtra("_id")
                        && data.hasExtra("videoYouTubeId")
                        && data.hasExtra("videoTitle")
                        && data.hasExtra("videoUrl")
                        && data.hasExtra("videoDescription")
                        && data.hasExtra("videoPlaylistId")) {

                    int _id = data.getIntExtra("_id", 0);
                    String videoYouTubeId = data.getStringExtra("videoYouTubeId");
                    String videoTitle = data.getStringExtra("videoTitle");
                    String videoUrl = data.getStringExtra("videoUrl");
                    String videoDescription = data.getStringExtra("videoDescription");
                    int playlistId = data.getIntExtra("videoPlaylistId", 0);

                    if (playlistId == curPlaylistItem.get_id()) {
                        videoPreviewAdapter.addItem(new VideoItem(_id, videoYouTubeId , videoTitle, videoUrl, videoDescription, playlistId));
                        videoPreviewAdapter.notifyDataSetChanged();
                        videoListFragmentCallback.resetToolbarText();
                    }
                }
            }
        } else if (requestCode == REQUEST_PLAY_VIDEO) {
            if (resultCode == RESULT_OK) {
                if (data != null
                        && data.hasExtra("deliveredCommand")
                        && data.hasExtra("playedVideoItem")) {

                    playedVideoItem = data.getParcelableExtra("playedVideoItem");
                    isCommandExist = true;
                    deliveredCommand = data.getParcelableExtra("deliveredCommand");
                }
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        isAutoScrolling = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Timber.d("onDestroy() 호출됨");

    }

    @Override
    public void onDetach() {
        Timber.d("onDetach() 호출됨");
        super.onDetach();

    }

}
