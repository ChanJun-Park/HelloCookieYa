package edu.inha.hellocookieya;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.HeaderViewListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;

import edu.inha.hellocookieya.api.RequestQueueSingleton;
import edu.inha.hellocookieya.databinding.ActivityMainBinding;
import edu.inha.hellocookieya.db.DBManager;
import edu.inha.hellocookieya.playlist.PlaylistItem;
import edu.inha.hellocookieya.speech.command.CommandEnum;
import edu.inha.hellocookieya.video.VideoItem;

import edu.inha.hellocookieya.video.VideoListFragmentCallback;
import timber.log.Timber;

import static edu.inha.hellocookieya.RequestCodeConstant.REQUEST_ADD_PLAYLIST;
import static edu.inha.hellocookieya.RequestCodeConstant.REQUEST_ADD_VIDEO;
import static edu.inha.hellocookieya.RequestCodeConstant.REQUEST_DELETE_PLAYLIST;
import static edu.inha.hellocookieya.RequestCodeConstant.REQUEST_EDIT_PLAYLIST;
import static edu.inha.hellocookieya.RequestCodeConstant.REQUEST_INIT_VOICE;

public class MainActivity extends HelloCookieYaActivity
        implements VideoListFragmentCallback, NavigationView.OnNavigationItemSelectedListener {

    private ActivityMainBinding binding;

    private Fragment curFragment;
    private VideoEmptyFragment videoEmptyFragment;
    private VideoListFragment videoListFragment;

    private ArrayList<PlaylistItem> playlistItems;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int curId = item.getItemId();
        switch (curId) {
            case R.id.menu_delete_playlist: {
                if (videoListFragment.getCurPlaylistItem().get_id() == 1) {
                    Toast.makeText(getApplicationContext(), "기본 재생목록은 삭제할 수 없습니다.", Toast.LENGTH_LONG).show();
                    break;
                }
                Intent intent = new Intent(getApplicationContext(), DeletePlaylistActivity.class);
                int playlistId = videoListFragment.getCurPlaylistItem().get_id();
                intent.putExtra("playlistId", playlistId);
                startActivityForResult(intent, REQUEST_DELETE_PLAYLIST);
                break;
            }
            case R.id.menu_edit_playlist: {
                if (videoListFragment.getCurPlaylistItem().get_id() == 1) {
                    Toast.makeText(getApplicationContext(), "기본 재생목록 이름은 수정할 수 없습니다.", Toast.LENGTH_LONG).show();
                    break;
                }
                Intent intent = new Intent(getApplicationContext(), EditPlaylistActivity.class);
                int playlistId = videoListFragment.getCurPlaylistItem().get_id();
                intent.putExtra("playlistId", playlistId);
                startActivityForResult(intent, REQUEST_EDIT_PLAYLIST);
                break;
            }
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        Timber.d("MainActivity onCreate()");

        DBManager.initialize(getApplicationContext());
        RequestQueueSingleton.initialize(getApplicationContext());

        initToolbarNavLayout();

        getPlaylistList();
        initPlaylistNavMenu();
        refreshNavMenu();

        videoEmptyFragment = new VideoEmptyFragment();
        videoListFragment = new VideoListFragment();
        videoListFragment.setCurPlaylistItem(playlistItems.get(0));
        resetToolbarText();

        getSupportFragmentManager().beginTransaction().add(R.id.container, videoListFragment).commit();
        curFragment = videoListFragment;

        resolveIntent();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        // 본 앱이 이미 실행되고 있는 상태에서 유튜브 앱의 공유하기 버튼 처리
        super.onNewIntent(intent);
        Timber.d("onNewIntent 호출됨");

        if (intent.hasExtra("sharedText")) {

            String sharedText = intent.getStringExtra("sharedText");

            if (videoListFragment.isVisible()) {
                videoListFragment.requestAddVideo(sharedText);
            }
            else if (videoEmptyFragment.isVisible()) {
                videoEmptyFragment.requestAddVideo(sharedText);
            }
            else {
                Timber.e("현재 활성화되어 있는 프래그먼트가 존재하지 않음");
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Timber.d("onActivityResult() 호출됨");

        if (requestCode == REQUEST_ADD_VIDEO) {
            if (resultCode == Activity.RESULT_OK) {
                if (curFragment instanceof VideoListFragment) {
                    int _id = data.getIntExtra("_id", 0);
                    String videoYouTubeId = data.getStringExtra("videoYouTubeId");
                    String videoTitle = data.getStringExtra("videoTitle");
                    String videoUrl = data.getStringExtra("videoUrl");
                    String videoDescription = data.getStringExtra("videoDescription");
                    int videoPlaylistId = data.getIntExtra("videoPlaylistId", 0);

                    videoListFragment.setReceivedItemFromMainActivity(new VideoItem(
                            _id, videoYouTubeId, videoTitle, videoUrl, videoDescription, videoPlaylistId
                    ));
                }
                else {
                    onVideoAdded();
                }
            }
        } else if (requestCode == REQUEST_INIT_VOICE) {
            if (resultCode == Activity.RESULT_OK) {
                Timber.d("사용자 음성인식 초기화 완료");
                SharedPreferences sharedPref = getSharedPreferences("settings", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean("isUserVoiceInitialized", true);
                editor.apply();

                initHotword();
            } else {
                Timber.d("사용자 음성인식 초기화 미루어짐");
            }
        } else if (requestCode == REQUEST_ADD_PLAYLIST) {
            if (resultCode == RESULT_OK) {
                if (data != null && data.hasExtra("_id") && data.hasExtra("playlistName")) {

                    String playlistName = data.getStringExtra("playlistName");
                    int _id = data.getIntExtra("_id", -1);

                    addPlaylistNavMenu(_id, playlistName);
                    refreshNavMenu();
                }
            }
        } else if (requestCode == REQUEST_EDIT_PLAYLIST) {
            if (resultCode == RESULT_OK) {
                if (data != null && data.hasExtra("_id") && data.hasExtra("playlistName")) {
                    String playlistName = data.getStringExtra("playlistName");
                    int _id = data.getIntExtra("_id", -1);

                    editPlaylistNavMenu(_id, playlistName);
                    resetToolbarText();
                }
            }
        } else if (requestCode == REQUEST_DELETE_PLAYLIST) {
            if (resultCode == RESULT_OK) {
                if (data != null && data.hasExtra("_id")) {
                    int _id = data.getIntExtra("_id", -1);

                    deletePlaylistNavMenu(_id);
                    PlaylistItem pItem = playlistItems.get(0);

                    if (curFragment instanceof VideoEmptyFragment) {
                        videoListFragment.setCurPlaylistItem(pItem);
                        curFragment = videoListFragment;
                        getSupportFragmentManager().beginTransaction().replace(R.id.container, videoListFragment).commit();
                    } else {
                        videoListFragment.resetPlaylist(pItem);
                        resetToolbarText();
                    }
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        processReturnedCommand();
    }

    @Override
    public void onFragmentCreateView() {
        resetToolbarText();
    }

    @Override
    public void onVideoDeleted() {
        resetToolbarText();
    }

    @Override
    protected void processReturnedCommand() {
        if (isCommandExist) {
            if (parsedCommand != null) {
                CommandEnum resultEnum = parsedCommand.getCmdType();
                switch (resultEnum) {
                    case COMMAND_PLAY_NTH_VIDEO:
                        if (curFragment instanceof VideoListFragment) {
                            videoListFragment.playNthVideo(parsedCommand.getParamValue());
                        } else {
                            Toast.makeText(getApplicationContext(), "재생할 영상이 없습니다", Toast.LENGTH_LONG).show();
                        }
                        break;
                    case COMMAND_DELETE_NTH_VIDEO: {
                        if (curFragment == videoListFragment) {
                            videoListFragment.deleteNthVideo(parsedCommand.getParamValue());
                            resetToolbarText();
                        } else {
                            Toast.makeText(getApplicationContext(), "삭제할 영상이 없습니다", Toast.LENGTH_LONG).show();
                        }
                        break;
                    }
                    case COMMAND_AUTO_SCROLL:
                        if (curFragment == videoListFragment) {
                            videoListFragment.startAutoScroll();
                            break;
                        }
                    case COMMAND_AUTO_SCROLL_STOP:
                        if (curFragment == videoListFragment) {
                            videoListFragment.stopAutoScroll();
                            break;
                        }
                    case COMMAND_QUIT: {
                        finishAffinity();
                        break;
                    }
                    default:
                        break;
                }
            }
            isCommandExist = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        recordingThread.releaseThread();
        DBManager.close();
        RequestQueueSingleton.close();
        Timber.d("MainActivity onDestroy()");
    }

    @Override
    public void onVideoEmpty() {
        getSupportFragmentManager().beginTransaction().replace(R.id.container, videoEmptyFragment).commit();
        curFragment = videoEmptyFragment;
    }

    @Override
    public void onVideoAdded() {
        Timber.d("onVideoAdded() 호출됨");
        getSupportFragmentManager().beginTransaction().replace(R.id.container, videoListFragment).commit();
        curFragment = videoListFragment;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.nav_init_voice) {
            Intent intent = new Intent(getApplicationContext(), AppInitializeActivity.class);
            startActivityForResult(intent, REQUEST_INIT_VOICE);
        } else if (itemId == R.id.addPlaylist) {
            Intent intent = new Intent(getApplicationContext(), AddPlaylistActivity.class);
            startActivityForResult(intent, REQUEST_ADD_PLAYLIST);
            Timber.d("새 재생목록 추가 선택됨");
            return true;
        } else {
            for (int i = 0; i < playlistItems.size(); i++) {
                PlaylistItem pItem = playlistItems.get(i);

                if (itemId == pItem.getPlaylistMenuItemId()) {
                    if (curFragment instanceof VideoEmptyFragment) {
                        videoListFragment.setCurPlaylistItem(pItem);
                        curFragment = videoListFragment;
                        getSupportFragmentManager().beginTransaction().replace(R.id.container, videoListFragment).commit();
                    } else {
                        videoListFragment.resetPlaylist(pItem);
                        resetToolbarText();
                    }
                    break;
                }
            }
        }

        binding.drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void initToolbarNavLayout() {
        setSupportActionBar(binding.toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, binding.drawerLayout, binding.toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        );
        binding.drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        binding.navView.setNavigationItemSelectedListener(this);
    }

    private void resolveIntent() {
        // 유튜브 앱에서 공유하기 눌렀을때, 본 앱이 실행되고 있지 않았을 때를 처리
        Intent intent = getIntent();
        if (intent.hasExtra("sharedText")) {

            String sharedText = intent.getStringExtra("sharedText");

            intent = new Intent(getApplicationContext(), AddVideoActivity.class);
            intent.putExtra("sharedText", sharedText);
            startActivityForResult(intent, REQUEST_ADD_VIDEO);
        }
    }

    private void getPlaylistList() {
        playlistItems = DBManager.getInstance(getApplicationContext())
                .getPlaylistList();
    }

    private void initPlaylistNavMenu() {
        for (int i = 0; i < playlistItems.size(); i++) {
            PlaylistItem item = playlistItems.get(i);
            item.setPlaylistMenuItemId(ViewCompat.generateViewId());
            binding.navView.getMenu().findItem(R.id.playlistList).getSubMenu()
                    .add(Menu.NONE, item.getPlaylistMenuItemId(), i, item.getPlaylistName())
                    .setIcon(R.drawable.ic_playlist);
        }
    }

    private void addPlaylistNavMenu(int _id, String playlistName) {
        PlaylistItem item = new PlaylistItem(_id, playlistName);
        item.setPlaylistMenuItemId(ViewCompat.generateViewId());

        Timber.d("새로 생긴 메뉴의 리소스 id : %s", item.getPlaylistMenuItemId());

        playlistItems.add(item);
        binding.navView.getMenu().findItem(R.id.playlistList).getSubMenu()
                .add(Menu.NONE, item.getPlaylistMenuItemId(), playlistItems.size(), playlistName)
                .setIcon(R.drawable.ic_playlist);
    }

    private void editPlaylistNavMenu(int _id, String playlistName) {

        for (PlaylistItem i : playlistItems) {
            if (i.get_id() == _id) {
                i.setPlaylistName(playlistName);
                binding.navView.getMenu().findItem(R.id.playlistList).getSubMenu()
                        .findItem(i.getPlaylistMenuItemId()).setTitle(playlistName);
                refreshNavMenu();
                break;
            }
        }
    }

    private void deletePlaylistNavMenu(int _id) {
        for (PlaylistItem i : playlistItems) {
            if (i.get_id() == _id) {
                binding.navView.getMenu().findItem(R.id.playlistList).getSubMenu()
                        .findItem(i.getPlaylistMenuItemId()).setVisible(false);
                playlistItems.remove(i);
                refreshNavMenu();
                break;
            }
        }
    }

    private void refreshNavMenu() {
        for (int i = 0, count = binding.navView.getChildCount(); i < count; i++) {
            final View child = binding.navView.getChildAt(i);
            if (child instanceof ListView) {
                final ListView menuView = (ListView) child;
                final HeaderViewListAdapter adapter = (HeaderViewListAdapter) menuView.getAdapter();
                final BaseAdapter wrapped = (BaseAdapter) adapter.getWrappedAdapter();
                wrapped.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void resetToolbarText() {
        PlaylistItem item = videoListFragment.getCurPlaylistItem();
        int itemCount = videoListFragment.getListItemCount();
        if (item != null) {
            String title = item.getPlaylistName();
            ActionBar actionBar = getSupportActionBar();
            if(actionBar != null)
            {
                actionBar.setTitle(title + " (" + itemCount + ") 개의 영상");
            }
        }
    }
}