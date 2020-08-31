package edu.inha.hellocookieya.intro;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import edu.inha.hellocookieya.api.snowboy.SnowboyApiConstants;
import edu.inha.hellocookieya.api.snowboy.ApiToken;
import edu.inha.hellocookieya.databinding.FragmentDownloadPmdlBinding;
import com.github.appintro.SlidePolicy;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

import static ai.kitt.snowboy.Constants.ACTIVE_PMDL;
import static ai.kitt.snowboy.Constants.DEFAULT_WORK_SPACE;

public class DownloadPmdlFragment extends Fragment
                                implements SlidePolicy, IntroFragmentInterface {

    private FragmentDownloadPmdlBinding binding;

    private String name = "Hello CookieYa";
    private String language = "ko";
    private String age_group ;
    private String gender;
    private String microphone = "macbook microphone";
    private String token = ApiToken.API_TOKEN;
    private final int BUFFER_SIZE = 100;

    private UserInfoProcessor userInfoProcessor;
    private Handler handler = new Handler();

    public DownloadPmdlFragment() {

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (userInfoProcessor == null) {
            userInfoProcessor = (UserInfoProcessor) getActivity();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        userInfoProcessor = null;
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDownloadPmdlBinding.inflate(inflater, container, false);
        ViewGroup rootView = binding.getRoot();

        if(userInfoProcessor.getUserGender().equals("남성")) {
            gender = "M";
        } else {
            gender = "F";
        }
        age_group = userInfoProcessor.getUserAgeGroup();

        binding.startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Timber.d("api 요청하기");
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            callSNOWBOYRestApi();
                        } catch (IOException e) {
                            Timber.e(e);
                        }
                    }
                });
                thread.start();
            }
        });

        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private String createApiParameter(String wavPath1, String wavPath2, String wavPath3) {
        Timber.i("키워드 이름 : %s", name);
        Timber.i("키워드 언어 : %s", language);
        Timber.i("유저 성별 : %s", gender);
        Timber.i("유저 나이대 : %s", age_group);

        File wavFile1 = new File(wavPath1);
        File wavFile2 = new File(wavPath2);
        File wavFile3 = new File(wavPath3);

        // 음성 녹음 파일 문자열 변환
        String wav1 = Base64.encodeToString(readFileToByteArray(wavFile1), Base64.NO_WRAP);
        String wav2 = Base64.encodeToString(readFileToByteArray(wavFile2), Base64.NO_WRAP);
        String wav3 = Base64.encodeToString(readFileToByteArray(wavFile3), Base64.NO_WRAP);


        StringBuilder voiceSamplesSB = new StringBuilder();
        voiceSamplesSB.append("[")
                .append("{\"wave\":\"").append(wav1).append("\"},")
                .append("{\"wave\":\"").append(wav2).append("\"},")
                .append("{\"wave\":\"").append(wav3).append("\"}")
                .append("]");

        // json 객체 생성
        StringBuilder jsonSB = new StringBuilder();
        jsonSB.append("{")
            .append("\"name\":\"").append(name).append("\",")
            .append("\"language\":\"").append(language).append("\",")
            .append("\"age_group\":\"").append(age_group).append("\",")
            .append("\"gender\":\"").append(gender).append("\",")
            .append("\"microphone\":\"").append(microphone).append("\",")
            .append("\"token\":\"").append(token).append("\",")
            .append("\"voice_samples\":").append(voiceSamplesSB.toString())
            .append("}");

        return jsonSB.toString();
    }

    private void callSNOWBOYRestApi() throws IOException {
        handler.post(new Runnable() {
            @Override
            public void run() {
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.startButton.setVisibility(View.GONE);
            }
        });

        String wavPath1 = DEFAULT_WORK_SPACE + "user_keyword_recording1.wav";
        String wavPath2 = DEFAULT_WORK_SPACE + "user_keyword_recording2.wav";
        String wavPath3 = DEFAULT_WORK_SPACE + "user_keyword_recording3.wav";

        String pmdlFilepath = DEFAULT_WORK_SPACE + ACTIVE_PMDL;

        String url = SnowboyApiConstants.apiHostAddress;
        URL urlObj =new URL(url);
        HttpURLConnection con = (HttpURLConnection) urlObj.openConnection();
        con.setDoOutput(true);
        con.setDoInput(true);
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestMethod("POST");

        OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
        wr.write(createApiParameter(wavPath1, wavPath2, wavPath3));
        wr.close();
        Timber.d("스노우보이 api 요청 보내짐");

        int HttpResult = con.getResponseCode();
        if (HttpResult == HttpURLConnection.HTTP_CREATED) {
            Timber.d("responseCode : %s", con.getResponseCode());
            Timber.d(con.getResponseMessage());
            Timber.d("Message Created, Reading response...");

            for (Map.Entry<String, List<String>> header : con.getHeaderFields().entrySet()) {
                for (String value : header.getValue()) {
                    Timber.d(header.getKey() + " : " + value);
                }
            }

            File pmdlFile = new File(pmdlFilepath);
            if (pmdlFile.exists()) {
                if (!pmdlFile.delete() || !pmdlFile.createNewFile()) {
                    Timber.e("pmdl 파일 갱신 실패");
                } else {
                    Timber.e("pmdl 파일 갱신 완료");
                }
            } else {
                Timber.e("pmdl 파일 없었음");
            }

            FileOutputStream outputStream =
                    new FileOutputStream(pmdlFile);
            InputStream inputStream = (InputStream) con.getContent();
            int read;
            byte[] bytes = new byte[BUFFER_SIZE];

            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
            Timber.d("pmdl 파일 생성중");
            outputStream.close();
            inputStream.close();
            con.disconnect();
            Timber.d("pmdl 파일 다운로드 완료!");

            handler.post(new Runnable() {
                @Override
                public void run() {
                    binding.titleTextView.setText("사용자 음성인식 초기화 완료!");
                    binding.progressBar.setVisibility(View.GONE);
                    binding.descriptionTextView.setVisibility(View.VISIBLE);
                }
            });
        } else {
            Timber.e(con.getResponseMessage());
            Timber.e("responseCode : %s", con.getResponseCode());
            BufferedReader br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            Timber.e(sb.toString());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    binding.alertDownloadErrorTextView.setText("오류가 발생했습니다.");
                    binding.startButton.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    private byte[] readFileToByteArray(File file){
        FileInputStream fis;

        byte[] bArray = new byte[(int) file.length()];
        try{
            fis = new FileInputStream(file);
            fis.read(bArray);
            fis.close();
        }catch(IOException ioExp){
            Timber.e(ioExp);
        }
        return bArray;
    }

    @Override
    public boolean isPolicyRespected() {
        String pmdlFilepath = DEFAULT_WORK_SPACE + "initialize.pmdl";
        File pmdl = new File(pmdlFilepath);

        return pmdl.exists();
    }

    @Override
    public void onUserIllegallyRequestedNextPage() {
        binding.alertDownloadErrorTextView.setText("사용자 음성 인식 설정 파일 다운로드 중입니다. 잠시만 기다려주세요");
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
}
