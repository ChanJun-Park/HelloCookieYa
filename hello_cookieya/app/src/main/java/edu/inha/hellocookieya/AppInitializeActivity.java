package edu.inha.hellocookieya;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import edu.inha.hellocookieya.intro.DoneFragment;
import edu.inha.hellocookieya.intro.DownloadPmdlFragment;
import edu.inha.hellocookieya.intro.IntroFragmentInterface;
import edu.inha.hellocookieya.intro.RecordUserVoiceFragment;
import edu.inha.hellocookieya.intro.UserInfoProcessor;
import edu.inha.hellocookieya.intro.UserVoiceInfoFragment;
import edu.inha.hellocookieya.intro.WelcomeFragment;
import com.github.appintro.AppIntro;

import org.jetbrains.annotations.Nullable;

public class AppInitializeActivity extends AppIntro
                        implements UserInfoProcessor {
    static {
        System.loadLibrary("snowboy-detect-android");
    }
    private String userGender = null;
    private String userAgeGroup = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setSystemBackButtonLocked(true);

        setColorDoneText(R.color.colorPrimary);
        setColorSkipButton(R.color.colorPrimary);
        setNextArrowColor(R.color.colorPrimary);
        setIndicatorColor(R.color.colorPrimary, R.color.colorAccent);
        addSlide(new WelcomeFragment());
        addSlide(new UserVoiceInfoFragment());
        addSlide(new RecordUserVoiceFragment());
        addSlide(new DownloadPmdlFragment());
        addSlide(new DoneFragment());
    }

    @Override
    protected void onDonePressed(@Nullable Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        Intent resultIntent = new Intent();
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    @Override
    protected void onSkipPressed(@Nullable Fragment currentFragment) {
        super.onSkipPressed(currentFragment);

        IntroFragmentInterface introFragment = (IntroFragmentInterface) currentFragment;
        if (introFragment != null)
            introFragment.onSkipButtonPressed();
    }

    @Override
    public void setUserGender(String gender) {
        userGender = gender;
    }

    @Override
    public void setUserAgeGroup(String ageGroup) {
        userAgeGroup = ageGroup;
    }

    @Override
    public String getUserGender() {
        return userGender;
    }

    @Override
    public String getUserAgeGroup() {
        return userAgeGroup;
    }
}
