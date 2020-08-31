package edu.inha.hellocookieya.speech;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import java.lang.ref.WeakReference;

import ai.kitt.snowboy.MsgEnum;
import ai.kitt.snowboy.audio.RecordingThread;
import timber.log.Timber;

public class KeywordSpottingService extends Service {

    private RecordingThread recordingThread;
    private boolean isKeywordDetected = false;
    private boolean shouldCallStopCallback = false;
    private KeywordSpottingServiceBinder kBinder = new KeywordSpottingServiceBinder();
    private RecordingThreadMessageHandler rHandler = new RecordingThreadMessageHandler(this);
    private OnKeywordDetectedCallback onKeywordDetectedCallback;

    public KeywordSpottingService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Timber.d("onCreate() 메소드 호출됨");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Timber.d("onStartCommand() 메소드 호출됨");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        recordingThread = null;
        Timber.d("onDestroy() 메소드 호출됨");
    }

    private void initHotword() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            if (recordingThread == null)
                recordingThread = RecordingThread.getInstance(rHandler);
        }
    }

    public void startDetecting() {
        if (recordingThread != null) {
            Timber.d("키워드 감지 시작");
            recordingThread.startRecording();
        }
    }

    public void stopDetecting() {
        if (recordingThread != null) {
            Timber.d("키워드 감지 종료");
            recordingThread.stopRecording();
        }
    }

    public class KeywordSpottingServiceBinder extends Binder {
        public KeywordSpottingService getService() { return KeywordSpottingService.this; }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Timber.d("onBind(), KeywordSpottingServiceBinder 반환");
        initHotword();
        return kBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Timber.d("onUnbind() 호출됨");
        return super.onUnbind(intent);
    }

    public interface OnKeywordDetectedCallback {
        void onKeywordDetected();
        void onRecordingStopped();
    }


    static class RecordingThreadMessageHandler extends Handler {
        private final WeakReference<KeywordSpottingService> kServiceRef;

        RecordingThreadMessageHandler(KeywordSpottingService service) {
            kServiceRef = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            MsgEnum message = MsgEnum.getMsgEnum(msg.what);
            KeywordSpottingService kService = kServiceRef.get();

            switch (message) {
                case MSG_ACTIVE:
                    Timber.d("키워드 감지됨");
                    if (kService != null && kService.onKeywordDetectedCallback != null) {
                        kService.stopDetecting();
                        kService.isKeywordDetected = true;
                    }
                    break;
                case MSG_INFO:
                    break;
                case MSG_VAD_SPEECH:
                    break;
                case MSG_VAD_NOSPEECH:
                    break;
                case MSG_ERROR:
                    break;
                case MSG_RECORDING_THREAD_STOPPED:
                    if (kService != null && kService.onKeywordDetectedCallback != null) {
                        if (kService.isKeywordDetected) {
                            kService.onKeywordDetectedCallback.onKeywordDetected();
                            kService.isKeywordDetected = false;
                        } else if (kService.shouldCallStopCallback) {
                            kService.onKeywordDetectedCallback.onRecordingStopped();
                            kService.shouldCallStopCallback = false;
                        }
                    }
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    }
}
