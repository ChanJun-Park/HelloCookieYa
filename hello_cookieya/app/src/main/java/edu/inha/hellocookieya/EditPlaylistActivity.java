package edu.inha.hellocookieya;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import edu.inha.hellocookieya.databinding.ActivityEditPlaylistBinding;
import edu.inha.hellocookieya.db.DBManager;

public class EditPlaylistActivity extends AppCompatActivity {

    private ActivityEditPlaylistBinding binding;
    private int playlistId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditPlaylistBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        resolveIntent();

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(view);

        setListener();
    }

    private void setListener() {
        binding.editPlaylistButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = binding.newPlayListNameEditText.getText().toString();
                if (name.equals("")) {
                    Toast.makeText(getApplicationContext(), "새 재생목록 이름을 입력하세요", Toast.LENGTH_SHORT).show();
                    return;
                }
                processEdit();
            }
        });

        binding.cancelEditingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processCancel();
            }
        });
    }

    private void processEdit() {
        Intent resultIntent = new Intent();

        String playlistName = binding.newPlayListNameEditText.getText().toString();
        DBManager.getInstance(getApplicationContext())
                .editPlaylist(this.playlistId, playlistName);

        resultIntent.putExtra("_id", this.playlistId);
        resultIntent.putExtra("playlistName", playlistName);

        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private void processCancel() {
        Intent resultIntent = new Intent();
        setResult(RESULT_CANCELED, resultIntent);
        finish();
    }

    private void resolveIntent() {
        Intent intent = getIntent();
        if (intent.hasExtra("playlistId")) {
            this.playlistId = intent.getIntExtra("playlistId", -1);
            if (this.playlistId == -1) {
                Toast.makeText(getApplicationContext(), "재생목록 이름 수정 에러", Toast.LENGTH_LONG).show();
                processCancel();
            }
        }
    }
}