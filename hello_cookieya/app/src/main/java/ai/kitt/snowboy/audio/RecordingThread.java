package ai.kitt.snowboy.audio;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Message;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import ai.kitt.snowboy.Constants;
import ai.kitt.snowboy.MsgEnum;
import ai.kitt.snowboy.SnowboyDetect;
import timber.log.Timber;

import static ai.kitt.snowboy.Constants.ACTIVE_PMDL;

public class RecordingThread {
    static {
        System.loadLibrary("snowboy-detect-android");
    }

    // 키워드 인식에 필요한 리소스 파일 경로
    private static final String commonResPath = Constants.DEFAULT_WORK_SPACE + Constants.ACTIVE_RES;
    private static final String activeModelPath = Constants.DEFAULT_WORK_SPACE + ACTIVE_PMDL;

    // 싱글톤 RecordingThread 객체
    private static RecordingThread instance;

    private boolean shouldThreadContinueRunning; // 스레드가 계속 실행되어야 하는지 체크하는 플래그
    private boolean shouldContinueRecording;     // 사운드 녹음이 계속 실행되어야 하는지 체크하는 플래그
    private boolean isAudioRecordStarted;        // 녹음 시작시 AudioRecorder 객체의 start 메소드를 한번만
                                                 // 호출하게 하는 플래그

    private Handler handler;    // 키워드를 인식한 경우 또는 녹음이 중지된 경우 메시지를 전달받을 액티비티의 핸들러
    private Thread thread;      // 사운드를 녹음하며 키워드를 인식할 쓰레드의 참조변수

    // 키워드 인식 기능을 수행하는 클래스
    private SnowboyDetect detector;

    // 싱글톤을 위한 private 생성자
    private RecordingThread(Handler handler) {
        this.handler = handler;
        detector = new SnowboyDetect(commonResPath, activeModelPath);
    }

    // 싱글톤 객체를 가져오는 메소드
    public static RecordingThread getInstance(Handler handler) {
        if (instance == null) {
            instance = new RecordingThread(handler);
        }

        // RecordingThread 의 메시지를 전달받을 핸들러 설정
        instance.handler = handler;
        return instance;
    }

    private void sendMessage(MsgEnum what, Object obj) {
        if (null != handler) {
            Message msg = handler.obtainMessage(what.ordinal(), obj);
            handler.sendMessage(msg);
        }
    }

    public void initThread() {
        shouldThreadContinueRunning = true;
        if (thread != null)
            return;
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                record();
            }
        });
        thread.start();
    }

    public void startRecording() {
        Timber.d("키워드 감지 시작됨");

        detector.setSensitivity("0.5");
//      detector.setAudioGain(1);
        detector.applyFrontend(true);

        initThread();

        shouldContinueRecording = true;
    }

    public void stopRecording() {
        Timber.d("키워드 감지 중지됨");
        shouldContinueRecording = false;
    }

    public void releaseThread() {
        stopRecording();
        shouldThreadContinueRunning = false;

        if (thread == null)
            return;
        thread = null;
    }

    private void record() {
        Timber.v("Start");
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);

        // Buffer size in bytes: for 0.1 second of audio
        int bufferSize = (int) (Constants.SAMPLE_RATE * 0.1 * 2);
        byte[] audioBuffer = new byte[bufferSize];

        AudioRecord record = new AudioRecord(
                MediaRecorder.AudioSource.DEFAULT,
                Constants.SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize);

        if (record.getState() != AudioRecord.STATE_INITIALIZED) {
            Timber.e("Audio Record can't initialize!");
            return;
        }

        while(shouldThreadContinueRunning) {

            while (shouldContinueRecording) {

                if (!isAudioRecordStarted) {
                    isAudioRecordStarted = true;
                    record.startRecording();
                    Timber.v("Start recording");
                }

                // 기기로 들어오는 사운드 녹음
                if (android.os.Build.VERSION.SDK_INT >= 23)
                    record.read(audioBuffer, 0, audioBuffer.length, AudioRecord.READ_BLOCKING);
                else
                    record.read(audioBuffer, 0, audioBuffer.length);

                // Converts to short array.
                short[] audioData = new short[audioBuffer.length / 2];
                ByteBuffer.wrap(audioBuffer).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(audioData);

                // 키워드가 발화되었는지 체크
                int result = detector.runDetection(audioData, audioData.length);
                if (result == -1) {
                    sendMessage(MsgEnum.MSG_ERROR, "Unknown Detection Error");
                } else if (result > 0) {
                    // 키워드가 인식된 경우
                    sendMessage(MsgEnum.MSG_ACTIVE, null);
                    Timber.i("Hotword %s detected!", Integer.toString(result));
                }
            } // while (shouldContinueRecording) 끝

            // STT 기능 수행을 위해서 키워드 인식을 위한 음성 녹음은 중지
            if (isAudioRecordStarted) {
                record.stop();
                isAudioRecordStarted = false;
                sendMessage(MsgEnum.MSG_RECORD_STOP, null);
            }
        } // while(shouldThreadContinueRunning) 끝

        record.release();
        Timber.d("종료된 쓰레드 : %s", Thread.currentThread().getId());
    }
}
