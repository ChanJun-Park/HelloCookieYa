package edu.inha.hellocookieya;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import edu.inha.hellocookieya.api.RequestQueueSingleton;
import edu.inha.hellocookieya.api.youtube.YoutubeApiConstants;
import edu.inha.hellocookieya.api.youtube.DeveloperKey;
import edu.inha.hellocookieya.api.youtube.ResponsePageInfo;
import edu.inha.hellocookieya.api.youtube.VideoInfo;
import edu.inha.hellocookieya.api.youtube.VideoListResponse;
import edu.inha.hellocookieya.api.youtube.VideoSnippet;
import edu.inha.hellocookieya.databinding.ActivityAddVideoBinding;
import edu.inha.hellocookieya.db.DBManager;
import edu.inha.hellocookieya.playlist.PlaylistItem;
import com.google.gson.Gson;

import java.util.ArrayList;

import timber.log.Timber;

public class AddVideoActivity extends AppCompatActivity {

    private ActivityAddVideoBinding binding;

    private ArrayList<PlaylistItem> playlistItems;
    private ArrayList<String> spinnerItems = new ArrayList<>();
    private int selectedPlaylistId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddVideoBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        Timber.d("onCreate 호출됨");

        playlistItems = DBManager.getInstance(getApplicationContext())
                .getPlaylistList();
        for (PlaylistItem item : playlistItems) {
            spinnerItems.add(item.getPlaylistName());
        }

        setListener();
        resolveIntent();
    }

    private void setListener() {
        ArrayAdapter<String> adapter1 = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, spinnerItems
        );
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.selectPlaylistSpinner.setAdapter(adapter1);
        binding.selectPlaylistSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedPlaylistId = playlistItems.get(position).get_id();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        binding.addVideoLinkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processSave();
            }
        });

        binding.cancelAddingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processCancel();
            }
        });

        binding.videoUrlEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                binding.videoUrlEditText.setEnabled(false);
            }
        });

        binding.titleEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                binding.addVideoTitleTextView.setText(s.toString());
            }
        });

        binding.descriptionEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                binding.addVideoDescTextView.setText(s.toString());
            }
        });
    }

    // AddVideoActivity 로 전달된 유튜브 영상의 url 정보를 처리
    private void resolveIntent() {
        Intent intent = getIntent();
        if (intent.hasExtra("sharedText")) {
            String sharedText = intent.getStringExtra("sharedText");
            if (sharedText != null) {
                String videoYouTubeId = getVideoYouTubeId(sharedText);
                if (videoYouTubeId != null) {
                    binding.videoUrlEditText.setText(sharedText);
                    processSearch();
                }
                else {
                    binding.addVideoLinkButton.setClickable(false);
                    binding.addVideoLinkButton.setEnabled(false);
                    binding.alertTextView.setText("유효한 유튜브 링크가 아닙니다. url 주소를 확인해주세요");
                }
            }
        }
    }

    // 유튜브 앱의 공유하기 버튼을 통해 얻은 유튜브 영상의 url 을 가지고 YouTube Data api 를 이용해서
    // 영상과 관련된 정보를 가져오는 메소드
    private void processSearch() {
        binding.addVideoCardView.setVisibility(View.GONE);
        binding.addVideoProgressBar.setVisibility(View.VISIBLE);

        String videoUrl = binding.videoUrlEditText.getEditableText().toString();
        String videoId = getVideoYouTubeId(videoUrl);

        // https://www.googleapis.com/youtube/v3/videos?id=tYM4oISacwY&key=[api 키]&part=snippet&fields=pageInfo,items(snippet(title,%20description))
        String requestUrl = YoutubeApiConstants.apiHostAddress + YoutubeApiConstants.requestResourceType +
                "id=" + videoId +
                "&key=" + DeveloperKey.DEVELOPMENT_KEY +
                YoutubeApiConstants.partialResourceRequest;

        StringRequest request = new StringRequest(
                Request.Method.GET,
                requestUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Timber.i("YouTube api 서버 응답 메시지 %s", response);
                        processVideoListResponse(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Timber.e(error);
                        processErrorResponse();
                    }
                }
        );

        request.setShouldCache(false);
        RequestQueueSingleton
                .getInstance(getApplicationContext())
                .getRequestQueue(getApplicationContext())
                .add(request);
    }

    private void processSave() {
        Intent resultIntent = new Intent();

        String videoUrl = binding.videoUrlEditText.getText().toString();
        String videoYouTubeId = getVideoYouTubeId(videoUrl);
        String videoTitle = binding.titleEditText.getText().toString();
        String videoDescription = binding.descriptionEditText.getText().toString();

        int _id = DBManager.getInstance(getApplicationContext())
                .addVideoLink(videoYouTubeId, videoTitle, videoUrl, videoDescription, selectedPlaylistId);

        resultIntent.putExtra("_id", _id);
        resultIntent.putExtra("videoYouTubeId", videoYouTubeId);
        resultIntent.putExtra("videoUrl", videoUrl);
        resultIntent.putExtra("videoTitle", videoTitle);
        resultIntent.putExtra("videoDescription", videoDescription);
        resultIntent.putExtra("videoPlaylistId", selectedPlaylistId);

        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private void processCancel() {
        setResult(RESULT_CANCELED);

        finish();
    }

    // YouTube Data api 서버로부터 응답받은 데이터를 처리하는 메소드
    private void processVideoListResponse(String response) {
        Gson gson = new Gson();
        VideoListResponse videoListResponse = gson.fromJson(response, VideoListResponse.class);
        if (videoListResponse == null) {

            binding.addVideoLinkButton.setClickable(false);
            binding.addVideoLinkButton.setEnabled(false);
            binding.alertTextView.setText("오류가 발생했습니다. 잠시 후에 다시 시도해주세요");
            return;
        }

        ResponsePageInfo responsePageInfo = videoListResponse.pageInfo;
        if (responsePageInfo == null) {
            binding.addVideoLinkButton.setClickable(false);
            binding.addVideoLinkButton.setEnabled(false);
            binding.alertTextView.setText("오류가 발생했습니다. 잠시 후에 다시 시도해주세요");
            return;
        }

        if (responsePageInfo.totalResults == 0) {
            binding.addVideoLinkButton.setClickable(false);
            binding.addVideoLinkButton.setEnabled(false);
            binding.alertTextView.setText("검색한 ID 에 해당하는 영상이 존재하지 않습니다. 영상 주소를 확인해주세요");
            return;
        }

        if (videoListResponse.items == null) {
            binding.addVideoLinkButton.setClickable(false);
            binding.addVideoLinkButton.setEnabled(false);
            binding.alertTextView.setText("오류가 발생했습니다. 잠시 후에 다시 시도해주세요");
            return;
        }

        VideoInfo info = videoListResponse.items.get(0);
        String videoId  = info.id;
        VideoSnippet snippet = info.snippet;

        if (snippet == null || videoId == null) {
            binding.addVideoLinkButton.setClickable(false);
            binding.addVideoLinkButton.setEnabled(false);
            binding.alertTextView.setText("오류가 발생했습니다. 잠시 후에 다시 시도해주세요");
            return;
        }

        binding.addVideoProgressBar.setVisibility(ProgressBar.GONE);
        binding.addVideoCardView.setVisibility(CardView.VISIBLE);
        Glide.with(binding.addVideoCardView)
                .load(YoutubeApiConstants.thumbnailApiHostAddress + videoId + YoutubeApiConstants.standardImage)
                .into(binding.addVideoThumbnailImageView);
        binding.addVideoTitleTextView.setText(snippet.title);
        binding.addVideoDescTextView.setText(snippet.description);

        binding.titleEditText.setText(snippet.title);
        binding.descriptionEditText.setText(snippet.description);

        binding.titleEditText.setEnabled(true);
        binding.descriptionEditText.setEnabled(true);
        binding.addVideoLinkButton.setClickable(true);
        binding.addVideoLinkButton.setEnabled(true);
    }

    private void processErrorResponse() {

    }


    private String getVideoYouTubeId(String videoUrl) {
        int index = videoUrl.indexOf("v=");
        String videoYouTubeId = null;
        if (index == -1) {
            if (videoUrl.contains("youtu.be/")) {
                index = videoUrl.indexOf("youtu.be/");
                index += 9;
                videoYouTubeId = videoUrl.substring(index);
            }
        }
        else {
            index += 2;
            videoYouTubeId = videoUrl.substring(index);
        }

        return videoYouTubeId;
    }
}
