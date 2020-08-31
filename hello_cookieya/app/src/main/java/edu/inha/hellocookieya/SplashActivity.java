package edu.inha.hellocookieya;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import edu.inha.hellocookieya.databinding.ActivitySplashBinding;
import com.pedro.library.AutoPermissions;
import com.pedro.library.AutoPermissionsListener;

import ai.kitt.snowboy.AppResCopy;
import timber.log.Timber;

import static edu.inha.hellocookieya.RequestCodeConstant.REQUEST_INIT_VOICE;

public class SplashActivity extends AppCompatActivity
        implements AutoPermissionsListener {
    static {
        System.loadLibrary("snowboy-detect-android");
    }

    private ActivitySplashBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Timber.uprootAll();

        if (BuildConfig.DEBUG && Timber.treeCount() == 0) {
            Timber.plant(new Timber.DebugTree());
        }

        Timber.d("onCreate() 메소드 호출됨");
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(view);

        // 아래에서 위로 올라오는 애니메이션 설정
        Animation bottomAnim = AnimationUtils.loadAnimation(this, R.anim.bottom_animation);

        binding.sloganTextView.setAnimation(bottomAnim);
        binding.appNameTextView.setAnimation(bottomAnim);

        // 위에서 아래로 내려가는 애니메이션 설정
        Animation topAnim = AnimationUtils.loadAnimation(this, R.anim.top_animation);
        binding.cookie.setAnimation(topAnim);

        // 5초 후에 권한 체크
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Timber.d("AutoPermissions.Companion.loadAllPermissions 호출하기 직전 ");
                AutoPermissions.Companion.loadAllPermissions(SplashActivity.this, 101);
            }
        }, 5000);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Timber.d("onRequestPermissionResult() 메소드 호출됨");
        AutoPermissions.Companion.parsePermissions(this, requestCode, permissions, this);
    }


    @Override
    public void onDenied(int requestCode, @NonNull String[] permissions) {
        Timber.d("onDenied() 메소드 호출됨");
        if (requestCode == 101) {
            if (permissions.length != 0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("안내")
                        .setMessage("본 앱의 동작을 위해서 오디오 녹음과 사진, 미디어, 파일에 대한 엑세스 권한이 필요합니다.\n" +
                                "앱을 재시작한 후에 오디오 녹음과 사진, 미디어, 파일에 대한 엑세스 권한을 허용해주세요\n\n" +
                                "(설정 > 애플리케이션 > 헬로 쿠키야! > 앱 권한 에서도 설정할 수 있습니다.)")
                        .setPositiveButton("종료", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
            else {
                startApp();
            }
        }
    }

    @Override
    public void onGranted(int requestCode, @NonNull String[] permissions) {
    }

    private void startApp() {
        copyAppFile();

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        // 다른 앱에서 공유하기 버튼 클릭 처리
        if (Intent.ACTION_SEND.equals(action) && "text/plain".equals(type)) {
            String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
            if (sharedText != null) {
                intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("sharedText", sharedText);

                startActivity(intent);
                finish();
            }
        } else if (checkFirstStarting()) {
            SharedPreferences sharedPref = getSharedPreferences("settings", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean("isFirstStart", false);
            editor.apply();

            intent = new Intent(getApplicationContext(), AppInitializeActivity.class);
            startActivityForResult(intent, REQUEST_INIT_VOICE);
        }
        else {
            intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_INIT_VOICE) {
            if (resultCode == Activity.RESULT_OK) {
                Timber.d("사용자 음성인식 초기화 완료");
                SharedPreferences sharedPref = getSharedPreferences("settings", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean("isUserVoiceInitialized", true);
                editor.apply();

                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
            } else {
                Timber.d("사용자 음성인식 초기화 미루어짐");
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
            }
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private boolean checkFirstStarting() {
        SharedPreferences sharedPref = getSharedPreferences("settings", Context.MODE_PRIVATE);
        return sharedPref.getBoolean("isFirstStart", true);
    }

    private void copyAppFile() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            AppResCopy.copyResFromAssetsToSD(this);
        }
    }
}
