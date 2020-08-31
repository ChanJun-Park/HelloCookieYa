package edu.inha.hellocookieya;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import edu.inha.hellocookieya.api.youtube.DeveloperKey;
import edu.inha.hellocookieya.databinding.ActivityPlayVideoBinding;
import edu.inha.hellocookieya.db.DBManager;
import edu.inha.hellocookieya.speech.command.CommandEnum;
import edu.inha.hellocookieya.speech.context.DialogContext;
import edu.inha.hellocookieya.video.OnBookmarkClickListener;
import edu.inha.hellocookieya.video.PlayVideoContent;
import edu.inha.hellocookieya.video.PlayVideoContentAdapter;
import edu.inha.hellocookieya.video.VideoItem;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerAndroidxFragment;
import com.google.android.youtube.player.YouTubePlayerFragment;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import timber.log.Timber;

import static androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE;
import static edu.inha.hellocookieya.RequestCodeConstant.REQUEST_COMMAND_PARAMETER;

public class PlayVideoActivity extends HelloCookieYaActivity
        implements YouTubePlayerAndroidxFragment.OnYouTubePlayerFragmentStateChanged {

    private ActivityPlayVideoBinding binding;

    // 유튜브 화면과 관련된 클래스 객체와 변수들
    private YouTubePlayerAndroidxFragment youTubePlayerAndroidxFragment;
    private YouTubePlayer player;
    private PlayVideoContentAdapter playVideoContentAdapter;
    private boolean isPlaying = true;
    private boolean isActivityPaused = false;
    private boolean isFullScreen = false;

    // 자동스크롤 기능을 위한 변수들
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

    // 재생할 유튜브 영상 정보를 담고있는 클래스 객체 멤버
    private VideoItem item;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.d("onCreate 호출됨");

        binding = ActivityPlayVideoBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        resolveIntent();
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Timber.d("onResume 호출됨");
        isActivityPaused = false;
        Timber.d("액티비티상태 조절 : %s", isActivityPaused);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // HelloCookieYaActivity 클래스의 onActivityResult 메소드에서
        // 파싱한 명령어를 parsedCommand, isCommandExist 변수들에 먼저 세팅한다.
        super.onActivityResult(requestCode, resultCode, data);
        Timber.d("onActivityResult() 호출됨");

        // 추가적인 음성명령어가 입력된 경우를 처리하는 코드
        if (requestCode == REQUEST_COMMAND_PARAMETER) {
            if (resultCode == RESULT_OK) {
                if (data != null
                        && data.hasExtra("command parameter")
                        && data.hasExtra("dialogContext")) {
                    DialogContext dialogContext = data.getParcelableExtra("dialogContext");
                    String commandParameter = data.getStringExtra("command parameter");

                    if (dialogContext == null)
                        return;
                    // 북마크 이름 입력과 관련된 추가 명령어 파라미터 입력인 경우
                    if (dialogContext.getType() == DialogContext.INPUT_BOOKMARK_NAME) {
                        DBManager.getInstance(getApplicationContext())
                                .editBookmarkTitle(dialogContext.getTargetEntityId(), commandParameter);
                        PlayVideoContent content = playVideoContentAdapter.getBookmarkItemFromID(dialogContext.getTargetEntityId());
                        content.setBookmarkDescription(commandParameter);
                        playVideoContentAdapter.notifyDataSetChanged();
                    }
                }
                isCommandExist = true;
                parsedCommand = null;
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Timber.d("onPause() 호출됨");
        isActivityPaused = true;
        Timber.d("액티비티상태 조절 : %s", isActivityPaused);
    }

    @Override
    public void onBackPressed() {
        if (player != null) {
            if (isFullScreen) {
                player.setFullscreen(false);
            } else {
                super.onBackPressed();
            }
        }
    }

    // 유튜브 영상을 재생하는 youTubePlayerAndroidxFragment 가 재시작되고 나서야
    // youTubePlayerAndroidxFragment 의 play(), pause()와 같은 메소드를
    // 호출할 수 있기 때문에 youTubePlayerAndroidxFragment 가 재시작될 때까지
    // 기다린 다음 인식한 음성 명령을 수행한다.
    // 이 메소드는 youTubePlayerAndroidxFragment 의 onResume() 메소드에서
    // activity.onFragmentResume(); 와 같은 형태로 호출된다.
    @Override
    public void onFragmentResume() {
        Timber.d("onFragmentResume() 호출됨");
        processReturnedCommand();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Timber.d("onDestroy() 호출됨");
    }

    @Override
    protected void processReturnedCommand() {
        if (isCommandExist) {
            if (parsedCommand != null) {
                CommandEnum resultEnum = parsedCommand.getCmdType();
                switch (resultEnum) {
                    case COMMAND_PLAY_PREV_VIDEO:
                    case COMMAND_PLAY_NEXT_VIDEO: {
                        player.release();
                        Intent resultIntent = new Intent();

                        resultIntent.putExtra("deliveredCommand", parsedCommand);
                        resultIntent.putExtra("playedVideoItem", item);

                        setResult(RESULT_OK, resultIntent);
                        finish();
                        break;
                    }
                    case COMMAND_PLAY_NTH_VIDEO:
                        Toast.makeText(getApplicationContext(), "현재 페이지에서 지원하지 않은 명령입니다.", Toast.LENGTH_LONG).show();
                        resumePlayState();
                        break;
                    case COMMAND_PLAY:
                        Toast.makeText(getApplicationContext(), "재생", Toast.LENGTH_LONG).show();
                        player.play();
                        break;
                    case COMMAND_PAUSE:
                        Toast.makeText(getApplicationContext(), "정지", Toast.LENGTH_LONG).show();
                        player.pause();
                        break;
                    case COMMAND_RELATIVE_SEEK: {
                        Toast.makeText(getApplicationContext(), "이동", Toast.LENGTH_LONG).show();

                        int amount = parsedCommand.getParamValue();
                        player.seekRelativeMillis(amount * 1000);
                        resumePlayState();
                        break;
                    }
                    case COMMAND_SEEK: {
                        int position = parsedCommand.getParamValue();
                        player.seekToMillis(position * 1000);
                        resumePlayState();
                        break;
                    }
                    case COMMAND_SEEK_TO_BOOKMARK: {
                        PlayVideoContent bookmark = playVideoContentAdapter.getBookmarkItem(parsedCommand.getParamValue());
                        if (bookmark == null) {
                            Toast.makeText(getApplicationContext(), "북마크가 존재하지 않습니다", Toast.LENGTH_LONG).show();
                        } else {
                            int bookmarkTimeMillis = bookmark.getBookmarkTime();
                            player.seekToMillis(bookmarkTimeMillis);
                        }
                        resumePlayState();
                        break;
                    }
                    case COMMAND_CREATE_BOOKMARK: {
                        int bookmarkId = createBookmark();

                        dialogContext = new DialogContext();
                        dialogContext.setType(DialogContext.INPUT_BOOKMARK_NAME);
                        dialogContext.setTargetEntityId(bookmarkId);

                        // 북마크와 관련된 추가적인 정보를 입력받기 위해서 STT 음성인식 기능 재실행
                        Intent intent = new Intent(getApplicationContext(), SpeechRecognitionActivity.class);
                        intent.putExtra("dialogContext", dialogContext);
                        intent.putExtra("promptMessage", "북마크 이름을 말해주세요");

                        expandedDialogIntent = intent;
                        isExpandedDialogExisted = true;
                        recordingThread.stopRecording();
                        break;
                    }
                    case COMMAND_AUTO_SCROLL:
                        startAutoScroll();
                        resumePlayState();
                        break;
                    case COMMAND_AUTO_SCROLL_STOP:
                        stopAutoScroll();
                        resumePlayState();
                        break;
                    case COMMAND_DELETE_NTH_BOOKMARK: {
                        PlayVideoContent bookmark = playVideoContentAdapter.getBookmarkItem(parsedCommand.getParamValue());
                        deleteBookmark(bookmark);
                        resumePlayState();
                        break;
                    }
                    case COMMAND_LANDSCAPE_MODE:
                        player.setFullscreen(true);
                        Timber.d("전체화면 명령어 파싱됨");
                        resumePlayState();
                        resumeScreenState();
                        break;
                    case COMMAND_PORTRAIT_MODE:
                        player.setFullscreen(false);
                        Timber.d("작은화면 명령어 파싱됨");
                        resumePlayState();
                        resumeScreenState();
                        break;
                    case COMMAND_MOVE_TO_LIST:
                        finish();
                        break;
                    case COMMAND_QUIT: {
                        finishAffinity();
                        break;
                    }
                    default:
                        resumePlayState();
                        break;
                }
            } else {
                resumePlayState();
            }
            isCommandExist = false;
        }
    }

    private void resolveIntent() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("videoItem")) {
            item = intent.getParcelableExtra("videoItem");
        }
    }

    private void initView() {
        initializeYouTubePlayer();

        layoutManager = new CustomScrollLinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL,false);
        binding.playVideoRecyclerView.setLayoutManager(layoutManager);

        playVideoContentAdapter = new PlayVideoContentAdapter(this);
        playVideoContentAdapter.setOnBookmarkClickListener(new OnBookmarkClickListener() {
            @Override
            public void onBookmarkClicked(int position) {
                PlayVideoContent item = playVideoContentAdapter.getItem(position);
                int bookmarkTimeMillis = item.getBookmarkTime();
                player.seekToMillis(bookmarkTimeMillis);
            }

            @Override
            public void onBookmarkDeleteClicked(final int position) {
                PlayVideoContent item = playVideoContentAdapter.getItem(position);
                int id = item.getBookmarkNumber();
                DBManager.getInstance(getApplicationContext())
                        .deleteBookmark(id);
                playVideoContentAdapter.deleteItem(position);
                playVideoContentAdapter.notifyDataSetChanged();
            }

            @Override
            public void onBookmarkNameEdited(int position, String editedName) {
                PlayVideoContent item = playVideoContentAdapter.getItem(position);
                item.setBookmarkDescription(editedName);
                DBManager.getInstance(getApplicationContext())
                        .editBookmarkTitle(item.getBookmarkNumber(), editedName);
            }

            @Override
            public void onBookmarkBreakpointClicked(int position) {}

            @Override
            public void onBookmarkRepeatClicked(int position) {}
        });

        playVideoContentAdapter.addItem(new PlayVideoContent(PlayVideoContent.TYPE_DESCRIPTION, item.getTitle(), item.getDescription()));
        playVideoContentAdapter.addItem(new PlayVideoContent(PlayVideoContent.TYPE_SEPARATOR));

        ArrayList<PlayVideoContent> bookmarkList = DBManager.getInstance(getApplicationContext())
                .getBookmarkList(item.get_id());
        playVideoContentAdapter.addItems(bookmarkList);
        playVideoContentAdapter.addItem(new PlayVideoContent(PlayVideoContent.TYPE_FOOTER));
        playVideoContentAdapter.sortItems();
        binding.playVideoRecyclerView.setAdapter(playVideoContentAdapter);

        binding.plusBookmarkFloatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createBookmark();
            }
        });
    }

    private void initializeYouTubePlayer() {
        youTubePlayerAndroidxFragment = new YouTubePlayerAndroidxFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(binding.youtubeViewContainer.getId(), youTubePlayerAndroidxFragment)
                .commitAllowingStateLoss();

        youTubePlayerAndroidxFragment.initialize(DeveloperKey.DEVELOPMENT_KEY, new YouTubePlayer.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean wasRestored) {
                player = youTubePlayer;
                if (!wasRestored) {
                    youTubePlayer.cueVideo(item.getVideo_youtube_id());
                }
                setYouTubePlayerListener();
            }

            @Override
            public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
                Timber.e("YouTubePlayer 초기화 실패");
            }
        });


    }

    private void setYouTubePlayerListener() {
        player.setPlayerStateChangeListener(new YouTubePlayer.PlayerStateChangeListener() {
            @Override
            public void onLoading() { }

            @Override
            public void onLoaded(String s) {
                Timber.d("PlayStateChangeListener - onLoaded() 호출됨");
                // 유튜브 영상 자동 실행
                player.play();
            }

            @Override
            public void onAdStarted() {}

            @Override
            public void onVideoStarted() {}

            @Override
            public void onVideoEnded() { }

            @Override
            public void onError(YouTubePlayer.ErrorReason errorReason) { }
        });

        player.setPlaybackEventListener(new YouTubePlayer.PlaybackEventListener() {
            @Override
            public void onPlaying() {
                isPlaying = true;
                Timber.d("play 상태 조절 : %s", isPlaying);
            }

            @Override
            public void onPaused() {
                if (!isActivityPaused && !isExpandedDialogExisted) {
                    isPlaying = false;
                    Timber.d("play 상태 조절 : %s", isPlaying);
                }
            }

            @Override
            public void onStopped() {
                if (!isActivityPaused && !isExpandedDialogExisted) {
                    isPlaying = false;
                    Timber.d("play 상태 조절 : %s", isPlaying);
                }
            }

            @Override
            public void onBuffering(boolean b) {
            }

            @Override
            public void onSeekTo(int i) {
            }
        });

        player.setOnFullscreenListener(new YouTubePlayer.OnFullscreenListener() {
            @Override
            public void onFullscreen(boolean b) {
                isFullScreen = b;
                setPlayerFullScreen(b);
                Timber.d("onFullscreen 호출됨");
            }
        });

        player.setFullscreenControlFlags(YouTubePlayer.FULLSCREEN_FLAG_CUSTOM_LAYOUT);
    }

    private void setPlayerFullScreen(boolean fullScreen) {
        if (fullScreen) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            hideSystemUI();
            Timber.d("landscape 모드");
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            showSystemUI();
            Timber.d("portrait 모드");
        }
    }

    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            decorView.setSystemUiVisibility(
                    // Set the content to appear under the system bars so that the
                    // content doesn't resize when the system bars hide and show.

                    // Hide the nav bar and status bar
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN);
        } else {
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN);
        }

    }

    // Shows the system bars by removing all the flags
    // except for the ones that make the content appear under the system bars.
    private void showSystemUI() {
        View decorView = getWindow().getDecorView();

        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    private int createBookmark() {
        int curTimeMillis = player.getCurrentTimeMillis();
        int id = DBManager.getInstance(getApplicationContext())
                .addBookmark(item.get_id(), curTimeMillis, "북마크 이름 입력");

        playVideoContentAdapter.addBookmarkItem(new PlayVideoContent(PlayVideoContent.TYPE_BOOKMARK, id, curTimeMillis, "북마크 이름 입력"));
        playVideoContentAdapter.notifyDataSetChanged();
        return id;
    }

    private void resumePlayState() {
        Timber.d("resumePlayState 호출됨");
        Timber.d("이전 플레이 상태 : %s", isPlaying);
        if (isPlaying)
            player.play();
        else
            player.pause();
    }

    private void resumeScreenState() {
        if (isFullScreen) {
            hideSystemUI();
        } else {
            showSystemUI();
        }
    }

    public void startAutoScroll() {
        if (!isListScrollable()) {
            return;
        }
        Timber.d("자동 스크롤 시작");
        binding.playVideoRecyclerView.removeOnScrollListener(onScrollListener);
        binding.playVideoRecyclerView.addOnScrollListener(onScrollListener);
        isAutoScrolling = true;

        if (layoutManager.findLastCompletelyVisibleItemPosition() == playVideoContentAdapter.getItemCount() - 1) {
            isScrollingUp = true;
            scrollUp();
        } else {
            isScrollingUp = false;
            scrollDown();
        }
    }

    private void scrollDown() {
        layoutManager.smoothScrollToPosition(
                binding.playVideoRecyclerView,
                new RecyclerView.State(),
                playVideoContentAdapter.getItemCount() - 1);
    }

    private void scrollUp() {
        layoutManager.smoothScrollToPosition(
                binding.playVideoRecyclerView,
                new RecyclerView.State(),
                0);
    }

    public void stopAutoScroll() {
        Timber.d("자동 스크롤 중지");
        isAutoScrolling = false;
        int currentPosition = layoutManager.findFirstCompletelyVisibleItemPosition();
        layoutManager.smoothScrollToPosition(
                binding.playVideoRecyclerView,
                new RecyclerView.State(),
                currentPosition
        );
    }

    private boolean isListScrollable() {
        return !(layoutManager.findLastCompletelyVisibleItemPosition() == playVideoContentAdapter.getItemCount() - 1)
                || !(layoutManager.findFirstCompletelyVisibleItemPosition() == 0);
    }

    private void deleteBookmark(PlayVideoContent bookmark) {
        if (bookmark == null) {
            Toast.makeText(getApplicationContext(), "북마크가 존재하지 않습니다", Toast.LENGTH_LONG).show();
        } else {
            DBManager.getInstance(getApplicationContext())
                    .deleteBookmark(bookmark.getBookmarkNumber());
            playVideoContentAdapter.deleteItem(bookmark.getAdapterIndex());
            playVideoContentAdapter.notifyDataSetChanged();

            Toast.makeText(getApplicationContext(), "북마크 제거됨", Toast.LENGTH_LONG).show();
        }
    }
}

