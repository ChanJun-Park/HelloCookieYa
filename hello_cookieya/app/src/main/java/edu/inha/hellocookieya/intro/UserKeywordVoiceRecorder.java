package edu.inha.hellocookieya.intro;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Message;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import ai.kitt.snowboy.Constants;
import ai.kitt.snowboy.MsgEnum;
import ai.kitt.snowboy.SnowboyDetect;
import timber.log.Timber;

import static ai.kitt.snowboy.Constants.ACTIVE_RES;
import static ai.kitt.snowboy.Constants.DEFAULT_WORK_SPACE;
import static ai.kitt.snowboy.Constants.PMDL_FOR_INITIALIZE;

public class UserKeywordVoiceRecorder {

    private final int AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;
    private final int SAMPLING_RATE = Constants.SAMPLE_RATE;
    private final int CHANNEL_TYPE = AudioFormat.CHANNEL_IN_MONO;
    private final int SPEACKER_CHANNEL_TYPE = AudioFormat.CHANNEL_OUT_MONO;
    private final int CHANNEL_COUNT = 1;
    private final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private final int BIT_PER_SAMPLE = 16;
    private final int RECORD_BUFFER_SIZE = (int)(SAMPLING_RATE * 0.01 * 2);
    private final int TRACK_BUFFER_SIZE = AudioTrack.getMinBufferSize(SAMPLING_RATE, SPEACKER_CHANNEL_TYPE, AUDIO_FORMAT);

    private Handler handler;

    private static final String strEnvWorkSpace = Constants.DEFAULT_WORK_SPACE;
    private static final String initializeModel = strEnvWorkSpace + PMDL_FOR_INITIALIZE;
    private static final String commonRes = strEnvWorkSpace + ACTIVE_RES;

    private SnowboyDetect detector = new SnowboyDetect(commonRes, initializeModel);

    private boolean isRecording = false;
    private Thread thread = null;
    private PCMToWAVGenerator pcmToWAVGenerator;
    private String resultFilename = null;

    private boolean isPlaying = false;
    private String playFilename = null;

    public UserKeywordVoiceRecorder(Handler handler) {
        this.handler = handler;
        pcmToWAVGenerator = new PCMToWAVGenerator(CHANNEL_COUNT, SAMPLING_RATE, BIT_PER_SAMPLE);
    }

    private void sendMessage(MsgEnum what, Object obj) {
        if (null != handler) {
            Message msg = handler.obtainMessage(what.ordinal(), obj);
            handler.sendMessage(msg);
        }
    }

    public boolean isExistRecordedFile(String fineName) {
        String waveFilepath = DEFAULT_WORK_SPACE + fineName + ".wav";
        File waveFile = new File(waveFilepath);
        return waveFile.exists();
    }

    public void startRecording(String resultFilename) {
        if (thread != null)
            return;
        this.resultFilename = resultFilename;
        isRecording = true;
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    record();
                } catch (Exception e) {
                    Timber.e(e);
                }
            }
        });
        thread.start();
    }

    public void stopRecording() {
        if (thread == null)
            return;
        isRecording = false;
        thread = null;
    }

    private void record() throws Exception {
        byte[] audioBuffer = new byte[RECORD_BUFFER_SIZE];

        String pcmFilepath = DEFAULT_WORK_SPACE + "record.pcm";
        File pcmFile = new File(pcmFilepath);
        FileOutputStream fos;
        fos = new FileOutputStream(pcmFile);

        AudioRecord audioRecord =
                new AudioRecord(
                AUDIO_SOURCE,
                SAMPLING_RATE,
                CHANNEL_TYPE,
                AUDIO_FORMAT,
                RECORD_BUFFER_SIZE);

        audioRecord.startRecording();

        while(isRecording) {
            int ret;
            if (android.os.Build.VERSION.SDK_INT >= 23)
                ret = audioRecord.read(audioBuffer, 0, audioBuffer.length, AudioRecord.READ_BLOCKING);
            else
                ret = audioRecord.read(audioBuffer, 0, audioBuffer.length);
            Timber.d("read bytes is %s", ret);

            // Converts to short array.
            short[] audioData = new short[audioBuffer.length / 2];
            ByteBuffer.wrap(audioBuffer).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(audioData);

            // Snowboy hotword detection.
            int result = detector.runDetection(audioData, audioData.length);

            if (result == -2) {
                // VAD NO SPEECH
            } else if (result == -1) {
                // VAD Unknown Detection Error
            } else if (result >= 0) {
                // VAD SPEECH
                try {
                    fos.write(audioBuffer, 0, RECORD_BUFFER_SIZE);    //  읽어온 readData 를 파일에 write 함
                }catch (IOException e){
                    Timber.e(e);
                }
            }
        }

        audioRecord.stop();
        audioRecord.release();

        fos.close();

        String waveFilepath = DEFAULT_WORK_SPACE + resultFilename + ".wav";
        File rawFile = new File(pcmFilepath);
        File waveFile = new File(waveFilepath);
        if (waveFile.exists()) {
            boolean deleteResult = waveFile.delete();
            if (!deleteResult) {
                throw new Exception("기존 녹음 파일 삭제 실패");
            }
        }
        try {
            pcmToWAVGenerator.rawToWave(rawFile, waveFile);
        } catch (IOException e) {
            Timber.e(e);
        }

        sendMessage(MsgEnum.MSG_RECORD_STOP, null);
    }

    public void playRecordedFile(String fileName) {
        if (thread != null)
            return;
        playFilename = fileName;
        isPlaying = true;

        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                play();
            }
        });
        thread.start();
    }

    private void play() {
        String playFilepath = DEFAULT_WORK_SPACE + playFilename + ".wav";
        byte[] writeData = new byte[TRACK_BUFFER_SIZE];
        FileInputStream fis;

        try {
            fis = new FileInputStream(playFilepath);
            DataInputStream dis = new DataInputStream(fis);
            try {
                // 헤더값 읽기
                dis.read(writeData, 0, 44);
            } catch (IOException e) {
                Timber.e(e);
            }

            AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                    SAMPLING_RATE,
                    SPEACKER_CHANNEL_TYPE,
                    AUDIO_FORMAT,
                    TRACK_BUFFER_SIZE,
                    AudioTrack.MODE_STREAM); // AudioTrack 생성
            audioTrack.play();  // write 하기 전에 play 를 먼저 수행해 주어야 함

            while(isPlaying) {
                try {
                    int ret = dis.read(writeData, 0, TRACK_BUFFER_SIZE);
                    if (ret <= 0) {
                        break;
                    }
                    audioTrack.write(writeData, 0, ret); // AudioTrack 에 write 를 하면 스피커로 송출됨
                } catch (IOException e) {
                    Timber.e(e);
                }

            }
            audioTrack.stop();
            audioTrack.release();

            try {
                dis.close();
                fis.close();
            } catch (IOException e) {
                Timber.e(e);
            }

            isPlaying = false;
            thread = null;

            sendMessage(MsgEnum.MSG_PLAY_STOP, null);
        } catch (FileNotFoundException e) {
            Timber.e(e);
        }
    }
}
