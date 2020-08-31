package edu.inha.hellocookieya;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import edu.inha.hellocookieya.databinding.ActivityDeletePlaylistBinding;
import edu.inha.hellocookieya.db.DBManager;

public class DeletePlaylistActivity extends AppCompatActivity {

    private ActivityDeletePlaylistBinding binding;
    private int playlistId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDeletePlaylistBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        resolveIntent();

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(view);

        setListener();
    }

    private void setListener() {
        binding.yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processDelete();
            }
        });

        binding.noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processCancel();
            }
        });
    }

    private void processDelete() {
        Intent resultIntent = new Intent();

        DBManager.getInstance(getApplicationContext())
                .deletePlaylist(this.playlistId);

        resultIntent.putExtra("_id", this.playlistId);

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
                Toast.makeText(getApplicationContext(), "재생목록 이름 삭제 에러", Toast.LENGTH_LONG).show();
                processCancel();
            }
        }
    }
}