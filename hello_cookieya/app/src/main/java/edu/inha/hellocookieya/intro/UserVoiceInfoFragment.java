package edu.inha.hellocookieya.intro;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import edu.inha.hellocookieya.databinding.FragmentUserVoiceInfoBinding;
import com.github.appintro.SlidePolicy;

import org.jetbrains.annotations.NotNull;

public class UserVoiceInfoFragment extends Fragment
                                    implements SlidePolicy, IntroFragmentInterface {

    private FragmentUserVoiceInfoBinding binding;
    private UserInfoProcessor userInfoProcessor;

    private final String[] items1 = { "여성", "남성" };
    private final String[] items2 = { "0_9", "10_19","20_29", "30_39", "40_49", "50_59", "60+" };

    public UserVoiceInfoFragment() {
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (userInfoProcessor == null)
            userInfoProcessor = (UserInfoProcessor) getActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        userInfoProcessor = null;
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentUserVoiceInfoBinding.inflate(inflater, container, false);
        ViewGroup rootView = binding.getRoot();

        ArrayAdapter<String> adapter1 = new ArrayAdapter<>(
                getActivity(), android.R.layout.simple_spinner_item, items1
        );
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.genderSpinner.setAdapter(adapter1);
        binding.genderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                userInfoProcessor.setUserGender(items1[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        ArrayAdapter<String> adapter2 = new ArrayAdapter<>(
                getActivity(), android.R.layout.simple_spinner_item, items2
        );
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.ageGroupSpinner.setAdapter(adapter2);
        binding.ageGroupSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                userInfoProcessor.setUserAgeGroup(items2[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        return rootView;
    }

    @Override
    public boolean isPolicyRespected() {
        return (userInfoProcessor.getUserAgeGroup() != null)
                && (userInfoProcessor.getUserGender() != null);
    }

    @Override
    public void onUserIllegallyRequestedNextPage() {
        binding.alertNoRecordedTextView.setText("이용자 정보를 입력해주세요");
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}