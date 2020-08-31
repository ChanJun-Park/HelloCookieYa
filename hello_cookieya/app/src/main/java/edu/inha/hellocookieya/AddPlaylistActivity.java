package edu.inha.hellocookieya;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import edu.inha.hellocookieya.databinding.ActivityAddPlaylistBinding;
import edu.inha.hellocookieya.db.DBManager;

public class AddPlaylistActivity extends AppCompatActivity {

    private ActivityAddPlaylistBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddPlaylistBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(view);

        setListener();
    }

    private void setListener() {
        binding.addPlaylistButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = binding.newPlayListNameEditText.getText().toString();
                if (name.equals("")) {
                    Toast.makeText(getApplicationContext(), "새 재생목록 이름을 입력하세요", Toast.LENGTH_SHORT).show();
                    return;
                }
                processSave();
            }
        });

        binding.cancelAddingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processCancel();
            }
        });
    }

    private void processSave() {
        Intent resultIntent = new Intent();

        String playlistName = binding.newPlayListNameEditText.getText().toString();
        int _id = DBManager.getInstance(getApplicationContext())
                .addPlaylist(playlistName);

        resultIntent.putExtra("_id", _id);
        resultIntent.putExtra("playlistName", playlistName);

        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private void processCancel() {
        Intent resultIntent = new Intent();
        setResult(RESULT_CANCELED, resultIntent);
        finish();
    }
}