package edu.inha.hellocookieya.intro;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import ai.kitt.snowboy.MsgEnum;
import edu.inha.hellocookieya.databinding.FragmentRecordUserVoiceBinding;
import com.github.appintro.SlidePolicy;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;

import timber.log.Timber;

public class RecordUserVoiceFragment extends Fragment
                                implements SlidePolicy, IntroFragmentInterface {

    private FragmentRecordUserVoiceBinding binding;

    private boolean isRecording1 = false;
    private boolean isRecording2 = false;
    private boolean isRecording3 = false;

    private boolean isPlaying1 = false;
    private boolean isPlaying2 = false;
    private boolean isPlaying3 = false;

    private VoiceRecorderMessageHandler handler = new VoiceRecorderMessageHandler(this);
    private UserKeywordVoiceRecorder recorder = new UserKeywordVoiceRecorder(handler);

    public RecordUserVoiceFragment() {
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentRecordUserVoiceBinding.inflate(inflater, container, false);
        ViewGroup rootView = binding.getRoot();
        setListener();

        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setListener() {
        binding.recordButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRecording2 || isRecording3) {
                    Toast.makeText(getActivity(), "다른 녹음을 먼저 끝내주세요.", Toast.LENGTH_LONG).show();
                    return;
                }
                else if (isPlaying1 || isPlaying2 || isPlaying3) {
                    Toast.makeText(getActivity(), "다른 재생을 먼저 끝내주세요.", Toast.LENGTH_LONG).show();
                    return;
                }

                if (isRecording1) {
                    recorder.stopRecording();
                    isRecording1 = false;
                    binding.recordButton1.setActivated(false);
                } else {
                    recorder.startRecording("user_keyword_recording1");
                    isRecording1 = true;
                    binding.recordButton1.setActivated(true);
                    setPlayButtonDisable();
                }
            }
        });

        binding.recordButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRecording1 || isRecording3) {
                    Toast.makeText(getActivity(), "다른 녹음을 먼저 끝내주세요.", Toast.LENGTH_LONG).show();
                    return;
                }
                else if (isPlaying1 || isPlaying2 || isPlaying3) {
                    Toast.makeText(getActivity(), "다른 재생을 먼저 끝내주세요.", Toast.LENGTH_LONG).show();
                    return;
                }

                if (isRecording2) {
                    recorder.stopRecording();
                    isRecording2 = false;
                    binding.recordButton2.setActivated(false);
                } else {
                    recorder.startRecording("user_keyword_recording2");
                    isRecording2 = true;
                    binding.recordButton2.setActivated(true);
                    setPlayButtonDisable();
                }
            }
        });

        binding.recordButton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRecording1 || isRecording2) {
                    Toast.makeText(getActivity(), "다른 녹음을 먼저 끝내주세요.", Toast.LENGTH_LONG).show();
                    return;
                }
                else if (isPlaying1 || isPlaying2 || isPlaying3) {
                    Toast.makeText(getActivity(), "다른 재생을 먼저 끝내주세요.", Toast.LENGTH_LONG).show();
                    return;
                }

                if (isRecording3) {
                    recorder.stopRecording();
                    isRecording3 = false;
                    binding.recordButton3.setActivated(false);
                } else {
                    recorder.startRecording("user_keyword_recording3");
                    isRecording3 = true;
                    binding.recordButton3.setActivated(true);
                    setPlayButtonDisable();
                }
            }
        });

        setPlayButtonDisable();
        restorePlayButtonStatus();
        binding.playButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRecording1 || isRecording2 || isRecording3) {
                    Toast.makeText(getActivity(), "다른 녹음을 먼저 끝내주세요.", Toast.LENGTH_LONG).show();
                    return;
                }
                else if (isPlaying2 || isPlaying3) {
                    Toast.makeText(getActivity(), "다른 재생을 먼저 끝내주세요.", Toast.LENGTH_LONG).show();
                    return;
                }

                if (!isPlaying1) {
                    isPlaying1 = true;
                    setPlayButtonDisable();
                    binding.playButton1.setEnabled(true);
                    binding.playButton1.setActivated(true);
                    recorder.playRecordedFile("user_keyword_recording1");
                }
            }
        });

        binding.playButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRecording1 || isRecording2 || isRecording3) {
                    Toast.makeText(getActivity(), "다른 녹음을 먼저 끝내주세요.", Toast.LENGTH_LONG).show();
                    return;
                }
                else if (isPlaying1 || isPlaying3) {
                    Toast.makeText(getActivity(), "다른 재생을 먼저 끝내주세요.", Toast.LENGTH_LONG).show();
                    return;
                }

                if (!isPlaying2){
                    isPlaying2 = true;
                    setPlayButtonDisable();
                    binding.playButton2.setEnabled(true);
                    binding.playButton2.setActivated(true);
                    recorder.playRecordedFile("user_keyword_recording2");
                }
            }
        });

        binding.playButton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRecording1 || isRecording2 || isRecording3) {
                    Toast.makeText(getActivity(), "다른 녹음을 먼저 끝내주세요.", Toast.LENGTH_LONG).show();
                    return;
                }
                else if (isPlaying1 || isPlaying2) {
                    Toast.makeText(getActivity(), "다른 재생을 먼저 끝내주세요.", Toast.LENGTH_LONG).show();
                    return;
                }

                if (!isPlaying3) {
                    isPlaying3 = true;
                    setPlayButtonDisable();
                    binding.playButton3.setEnabled(true);
                    binding.playButton3.setActivated(true);
                    recorder.playRecordedFile("user_keyword_recording3");
                }
            }
        });
    }

    private void restorePlayButtonStatus() {
        if (recorder.isExistRecordedFile("user_keyword_recording1")) {
            Timber.d("user_keyword_recording1 있음");
            binding.playButton1.setEnabled(true);
        }
        if (recorder.isExistRecordedFile("user_keyword_recording2")) {
            Timber.d("user_keyword_recording2 있음");
            binding.playButton2.setEnabled(true);
        }
        if (recorder.isExistRecordedFile("user_keyword_recording3")) {
            Timber.d("user_keyword_recording3 있음");
            binding.playButton3.setEnabled(true);
        }

        setPlayButtonUnActivated();
    }

    private void setPlayingStateFalse() {
        isPlaying1 = isPlaying2 = isPlaying3 = false;
    }

    private void setPlayButtonDisable() {
        binding.playButton1.setEnabled(false);
        binding.playButton2.setEnabled(false);
        binding.playButton3.setEnabled(false);
    }

    private void setPlayButtonUnActivated() {
        binding.playButton1.setActivated(false);
        binding.playButton2.setActivated(false);
        binding.playButton3.setActivated(false);
    }

    @Override
    public boolean isPolicyRespected() {
        return recorder.isExistRecordedFile("user_keyword_recording1") &&
                recorder.isExistRecordedFile("user_keyword_recording2") &&
                recorder.isExistRecordedFile("user_keyword_recording3");
    }

    @Override
    public void onUserIllegallyRequestedNextPage() {
        binding.alertNoRecordedTextView.setText("헬로 쿠키야를 3번 녹음하셔야 합니다.");
    }

    @Override
    public void onSkipButtonPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("안내")
                .setMessage("초기 설정을 건너뛰실 경우 음성인식 기능을 사용할 수 없습니다. 메뉴 > 설정에서 나중에 " +
                        "사용자 음성 설정을 진행할 수 있습니다. 건너뛰시겠습니까?")
                .setPositiveButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getActivity().finish();
                    }
                })
                .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private static class VoiceRecorderMessageHandler extends Handler {
        private final WeakReference<RecordUserVoiceFragment> ref;

        VoiceRecorderMessageHandler(RecordUserVoiceFragment fragment) {
            this.ref = new WeakReference<>(fragment);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            MsgEnum message = MsgEnum.getMsgEnum(msg.what);
            RecordUserVoiceFragment fragment = ref.get();

            if (message == MsgEnum.MSG_PLAY_STOP) {
                Timber.d("MSG_PLAYING_STOPPED 전달받음");
                fragment.setPlayingStateFalse();
                fragment.restorePlayButtonStatus();
            }
            else if (message == MsgEnum.MSG_RECORD_STOP) {
                Timber.d("MSG_RECORDING_STOPPED 전달받음");
                fragment.restorePlayButtonStatus();
            }
            else {
                super.handleMessage(msg);
            }
        }
    }
}
