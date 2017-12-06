package findmyphone.sync.bytes.findmyphone;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.codemybrainsout.onboarder.AhoyOnboarderActivity;
import com.codemybrainsout.onboarder.AhoyOnboarderCard;

import java.util.ArrayList;
import java.util.List;

import findmyphone.sync.bytes.findmyphone.utils.Writer;

public class OnboardingActivity extends AhoyOnboarderActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_onboarding);

        //Start creating the pages
        AhoyOnboarderCard card1 = new AhoyOnboarderCard("Welcome", "Let's get started", R.drawable.web_hi_res_512);
        AhoyOnboarderCard card2 = new AhoyOnboarderCard("Set PIN", "Create a random 4 digit pin, use it to send sms to find your phone", R.drawable.set_pin_info);
        AhoyOnboarderCard card3 = new AhoyOnboarderCard("Send Message", "Send a SMS from any other phone using the pin, to make the phone ring", R.drawable.sms_info);
        AhoyOnboarderCard card4 = new AhoyOnboarderCard("Stop Ringing", "Stop the phone ringing, using the notification", R.drawable.notification_info);

        card1.setBackgroundColor(R.color.black_transparent);
        card2.setBackgroundColor(R.color.black_transparent);
        card3.setBackgroundColor(R.color.black_transparent);
        card4.setBackgroundColor(R.color.black_transparent);

        card1.setIconLayoutParams(512,512,0,0,0,0);
        card2.setIconLayoutParams(861,516, 0,0,0,0);
        card3.setIconLayoutParams(608, 760, 0,0,0,0);
        card4.setIconLayoutParams(768, 587, 0,0,0,0);

        List<AhoyOnboarderCard> pages = new ArrayList<>(4);
        pages.add(card1);
        pages.add(card2);
        pages.add(card3);
        pages.add(card4);

        for (AhoyOnboarderCard page : pages) {
            page.setTitleColor(R.color.white);
            page.setDescriptionColor(R.color.grey_200);
        }

        showNavigationControls(true);
        setGradientBackground();
        setFinishButtonDrawableStyle(ContextCompat.getDrawable(this, R.drawable.rounded_button));

        setOnboardPages(pages);
    }

    @Override
    public void onFinishButtonPressed() {
        Writer.isFirstTime(this, false);
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        this.finish();
    }
}
