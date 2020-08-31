package edu.inha.hellocookieya.intro;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import edu.inha.hellocookieya.R;

public class WelcomeFragment extends Fragment
                        implements IntroFragmentInterface {

    public WelcomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_welcome, container, false);
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
