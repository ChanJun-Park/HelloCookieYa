package edu.inha.hellocookieya;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.lang.ref.WeakReference;

import ai.kitt.snowboy.MsgEnum;
import ai.kitt.snowboy.audio.RecordingThread;
import edu.inha.hellocookieya.speech.command.Command;
import edu.inha.hellocookieya.speech.context.DialogContext;
import timber.log.Timber;

import static ai.kitt.snowboy.Constants.ACTIVE_PMDL;
import static ai.kitt.snowboy.Constants.DEFAULT_WORK_SPACE;
import static edu.inha.hellocookieya.RequestCodeConstant.REQUEST_COMMAND_PARAMETER;
import static edu.inha.hellocookieya.RequestCodeConstant.REQUEST_SPEECH_RECOGNITION;

public abstract class HelloCookieYaActivity extends AppCompatActivity {

    // 키워드 인식 쓰레드에서 전달받은 메시지를 처리할 핸들러
    protected RecordingThreadMessageHandler handler = new RecordingThreadMessageHandler(this);
    protected RecordingThread recordingThread;

    private boolean isKeywordDetected = false;

    // 추가적인 음성 명령어 입력이 필요한 경우(북마크)를 처리하는 변수들
    protected boolean isExpandedDialogExisted = false;
    protected Intent expandedDialogIntent;
    protected DialogContext dialogContext;

    // 명령어 파싱을 통해서 인식한 명령어를 처리하는 변수들
    protected boolean isCommandExist = false;
    protected Command parsedCommand;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initHotword();
    }

    protected void initHotword() {
        // External Storage 쓰기 권한, 마이크 녹음 권한
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {

            // 사용자 키워드 인식 초기화 확인
            if (checkForPmdl() && checkVoiceInitialized()) {
                recordingThread = RecordingThread.getInstance(handler);
                recordingThread.initThread();
            }
        }
    }

    // 사용자 키워드 인식을 위한 pmdl 파일이 존재하는지 확인
    protected boolean checkForPmdl() {
        String pmdlFilepath = DEFAULT_WORK_SPACE + ACTIVE_PMDL;
        Timber.d("checkForPmdl에서 pmdl 파일 경로 %s", pmdlFilepath);
        File pmdlFile = new File(pmdlFilepath);
        return pmdlFile.exists();
    }

    // 사용자가 음성인식 초기화 과정을 거쳤는지 확인
    protected boolean checkVoiceInitialized() {
        SharedPreferences sharedPref = getSharedPreferences("settings", Context.MODE_PRIVATE);
        return sharedPref.getBoolean("isUserVoiceInitialized", false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Timber.d("onResume() 호출됨");
        // 다른 액티비티가 실행되었다가 돌아올 경우 MessageHandler 다시 설정
        initHotword();
        if (recordingThread != null)
            recordingThread.startRecording();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Timber.d("onPause() 호출됨");
        if (recordingThread != null)
            recordingThread.stopRecording();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Timber.d("onActivityResult() 호출됨");

        // STT 를 이용해서 문자열로 변환한 음성 명령어를 파싱한 결과 확인
        if (requestCode == REQUEST_SPEECH_RECOGNITION) {
            if (resultCode == RESULT_OK) {
                Timber.d("명령어로 변환됨");
                if (data.hasExtra("recognition result")) {
                    isCommandExist = true;
                    parsedCommand = data.getParcelableExtra("recognition result");
                    Timber.d("parseCommand type : %s", parsedCommand.getCommand());
                }
            }
            else if (resultCode == RESULT_CANCELED) {
                String result = data.getStringExtra("recognition result");
                if (result != null) {
                    Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
                    isCommandExist = true;
                    parsedCommand = null;
                }
            }
        }
    }

    // 이 클래스를 상속하는 하위 클래스에서 인식한 음성 명령어를 처리하게 만든다.
    protected abstract void processReturnedCommand();

    static class RecordingThreadMessageHandler extends Handler {
        private final WeakReference<HelloCookieYaActivity> helloCookieYaActivityWeakReference;

        RecordingThreadMessageHandler(HelloCookieYaActivity activity) {
            helloCookieYaActivityWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            MsgEnum message = MsgEnum.getMsgEnum(msg.what);
            HelloCookieYaActivity activity = helloCookieYaActivityWeakReference.get();

            switch (message) {
                case MSG_ACTIVE:
                    Timber.d("키워드 감지됨");
                    activity.isKeywordDetected = true;

                    // STT 기능 수행전 키워드 인식 쓰레드에서의 녹음은 중지시키기
                    activity.recordingThread.stopRecording();
                    break;
                case MSG_ERROR:
                    break;
                case MSG_RECORD_STOP:
                    Timber.d("녹음 중지됨");

                    // 액티비티의 onPause() 호출에 의해서가 아닌,
                    // 키워드가 인식되어서 녹음이 중지된 경우만 체크
                    if (activity.isKeywordDetected) {
                        activity.isKeywordDetected = false;

                        // STT 및 음성 명령어 파싱 액티비티 실행
                        Intent intent = new Intent(activity.getApplicationContext(), SpeechRecognitionActivity.class);
                        intent.putExtra("promptMessage", "멍멍(무엇을 도와드릴까요?)");
                        activity.startActivityForResult(intent, REQUEST_SPEECH_RECOGNITION);
                    } else if (activity.isExpandedDialogExisted) {
                        activity.isExpandedDialogExisted = false;

                        // 추가적인 음성 명령어 입력이 필요한 경우(북마크)
                        activity.startActivityForResult(activity.expandedDialogIntent, REQUEST_COMMAND_PARAMETER);
                    }
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    }
}
