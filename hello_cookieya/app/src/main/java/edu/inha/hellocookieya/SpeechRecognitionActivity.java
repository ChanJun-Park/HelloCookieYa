package edu.inha.hellocookieya;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import edu.inha.hellocookieya.databinding.ActivitySpeechRecognitionBinding;
import edu.inha.hellocookieya.speech.command.Command;
import edu.inha.hellocookieya.speech.command.CommandParser;
import edu.inha.hellocookieya.speech.context.DialogContext;

import java.util.ArrayList;

import timber.log.Timber;

public class SpeechRecognitionActivity extends AppCompatActivity {

    private ActivitySpeechRecognitionBinding binding;

    private String promptMessage;
    private DialogContext dialogContext;

    private Handler handler = new Handler();
    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognitionIntent;
    private RecognitionListener recognitionListener = new RecognitionListener() {

        boolean isOnResultProcessed = false;

        @Override
        public void onReadyForSpeech(Bundle params) {
            binding.promptTextView.setText(promptMessage);
        }

        @Override
        public void onBeginningOfSpeech() {

        }

        @Override
        public void onRmsChanged(float rmsdB) {

        }

        @Override
        public void onBufferReceived(byte[] buffer) {

        }

        @Override
        public void onEndOfSpeech() {

        }

        @Override
        public void onError(int error) {
            String message;

            switch (error) {
                case SpeechRecognizer.ERROR_AUDIO:
                    message = "오디오 에러";
                    break;
                case SpeechRecognizer.ERROR_CLIENT:
                    message = "클라이언트 에러";
                    break;
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                    message = "퍼미션 없음";
                    break;
                case SpeechRecognizer.ERROR_NETWORK:
                    message = "네트워크 에러";
                    break;
                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                    message = "네트웍 타임아웃";
                    break;
                case SpeechRecognizer.ERROR_NO_MATCH:
                    message = "찾을 수 없음";
                    break;
                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                    message = "RECOGNIZER가 바쁨";
                    break;
                case SpeechRecognizer.ERROR_SERVER:
                    message = "서버가 이상함";
                    break;
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    message = "말하는 시간초과";
                    break;
                default:
                    message = "알 수 없는 오류임";
                    break;
            }

            Timber.d("에러가 발생하였습니다. : %s", message);
            Intent resultIntent = new Intent();
            resultIntent.putExtra("recognition result", message);
            setResult(RESULT_CANCELED, resultIntent);
            finish();

            overridePendingTransition(R.anim.no_animation, R.anim.slide_down_animation);
        }

        @Override
        public void onResults(Bundle results) {
            if (!isOnResultProcessed) {
                isOnResultProcessed = true;
                final ArrayList<String> matches =
                        results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

                if (matches != null) {
                    binding.userTextView.setText(matches.get(0));
                    Timber.d("음성인식된 문자열 : %s", matches.get(0));

                    if (dialogContext != null) {
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                Intent resultIntent = new Intent();
                                resultIntent.putExtra("command parameter", matches.get(0));
                                resultIntent.putExtra("dialogContext", dialogContext);
                                setResult(RESULT_OK, resultIntent);
                                finish();

                                overridePendingTransition(R.anim.no_animation, R.anim.slide_down_animation);
                                isOnResultProcessed = false;
                            }
                        }, 1500);
                    } else {
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                Intent resultIntent = new Intent();
                                resultIntent.putExtra("recognition result", parseRecognitionResult(matches.get(0)));
                                setResult(RESULT_OK, resultIntent);
                                finish();

                                overridePendingTransition(R.anim.no_animation, R.anim.slide_down_animation);
                                isOnResultProcessed = false;
                            }
                        }, 1500);
                    }
                }
            }
        }

        @Override
        public void onPartialResults(Bundle partialResults) {
            ArrayList<String> matches =
                    partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

            if (matches != null) {
                binding.userTextView.setText(matches.get(0));
            }
        }

        @Override
        public void onEvent(int eventType, Bundle params) {

        }
    };

    private Command parseRecognitionResult(String str) {
        CommandParser parser = CommandParser.getInstance();
        return parser.parseCommand(str);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySpeechRecognitionBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        hideSystemUI();
        setContentView(view);

        resolveIntent();

        speechRecognitionIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognitionIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");
        speechRecognitionIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(recognitionListener);
        speechRecognizer.startListening(speechRecognitionIntent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        speechRecognizer.destroy();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void resolveIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra("promptMessage")) {
                promptMessage = intent.getStringExtra("promptMessage");
            }
            if (intent.hasExtra("dialogContext")) {
                dialogContext = intent.getParcelableExtra("dialogContext");
            }
        }
    }

    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            decorView.setSystemUiVisibility(
                    // Set the content to appear under the system bars so that the
                    // content doesn't resize when the system bars hide and show.

                    // Hide the nav bar and status bar
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN);
        } else {
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                  | View.SYSTEM_UI_FLAG_FULLSCREEN);
        }

    }
}
