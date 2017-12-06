package findmyphone.sync.bytes.findmyphone;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.startapp.android.publish.ads.banner.Banner;
import com.startapp.android.publish.ads.banner.BannerListener;
import com.startapp.android.publish.ads.banner.bannerstandard.BannerStandard;
import com.startapp.android.publish.adsCommon.SDKAdPreferences;
import com.startapp.android.publish.adsCommon.StartAppAd;
import com.startapp.android.publish.adsCommon.StartAppSDK;
import com.tapadoo.alerter.Alerter;

import findmyphone.sync.bytes.findmyphone.utils.Constants;
import findmyphone.sync.bytes.findmyphone.utils.Reader;
import findmyphone.sync.bytes.findmyphone.utils.Writer;
import com.facebook.ads.*;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener, BannerListener {

    private static final String TAG = MainActivity.class.getCanonicalName();

    Button changePin;
    TextView currentPinTextView;
    CheckBox locationCheckbox;

    private NativeAd nativeAd;

    ImageView nativeAdIcon;
    TextView nativeAdTitle ,nativeAdSocialContext, nativeAdBody;
    MediaView nativeAdMedia;
    Button nativeAdCallToAction;

    CardView ads_CardView;

    Banner banner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StartAppSDK.init(this, Constants.STARTAPP_APP_ID,false);
        StartAppAd.disableSplash();
        StartAppAd.disableAutoInterstitial();

        setContentView(R.layout.activity_main);

        if(Reader.readIsFirstTime(this))
        {
            //Start onboarding activity
            Intent intent = new Intent(this, OnboardingActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            this.finish();
        }

        currentPinTextView = findViewById(R.id.pin_textView);
        locationCheckbox = findViewById(R.id.location_checkBox);

        //ads support
        nativeAdIcon = (ImageView) findViewById(R.id.native_ad_icon);
        nativeAdTitle = (TextView) findViewById(R.id.native_ad_title);
        nativeAdMedia = (MediaView) findViewById(R.id.native_ad_media);
        nativeAdSocialContext = (TextView) findViewById(R.id.native_ad_social_context);
        nativeAdBody = (TextView) findViewById(R.id.native_ad_body);
        nativeAdCallToAction = (Button) findViewById(R.id.native_ad_call_to_action);

        ads_CardView = findViewById(R.id.ads_card_view);
        ads_CardView.setVisibility(View.INVISIBLE);

        changePin = findViewById(R.id.changePin_button);
        changePin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new MaterialDialog.Builder(MainActivity.this)
                        .title(R.string.text_input_title)
                        .content(R.string.text_input_hint)
                        .inputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_NORMAL)
                        .input(R.string.text_input_hint, R.string.text_input_prefill, new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(MaterialDialog dialog, CharSequence input) {
                                Log.d(TAG," PIN: "+input);
                                if(input.toString().matches("[0-9]{4}")){
                                    Writer.writePIN(MainActivity.this, input.toString());
                                    currentPinTextView.setText(input);
                                }
                                else
                                {
                                    if(!Alerter.isShowing()) {
                                        Alerter.create(MainActivity.this)
                                                .setTitle(R.string.text_input_title)
                                                .setText(R.string.text_input_error_message)
                                                .enableSwipeToDismiss()
                                                .enableVibration(true)
                                                .setDuration(3000)
                                                .setBackgroundColorRes(R.color.colorPrimary)
                                                .show();
                                    }
                                }
                            }
                        }).show();
            }
        });

        banner = findViewById(R.id.startAppBanner);
        banner.setVisibility(View.INVISIBLE);
        banner.setBannerListener(this);
        banner.hideBanner();


        //AdSettings.addTestDevice("a31c1bdeb709aed22a8aca018766c7e9");
        showNativeAd();
    }


    @Override
    public void onResume()
    {
        super.onResume();
        String pin = Reader.readPIN(this);
        if(pin == null)
        {
            Writer.writePIN(this, "1234");
            Writer.writePinNotSet(this, false);
            pin = "1234";
        }
        currentPinTextView.setText(pin);
    }


    @Override
    public void onCheckedChanged(CompoundButton button, boolean isChecked)
    {
        Writer.writeSendLocation(MainActivity.this, isChecked);
    }


    private void showNativeAd() {

        nativeAd = new NativeAd(this, Constants.YOUR_PLACEMENT_ID);
        nativeAd.setAdListener(new AdListener() {

            @Override
            public void onError(Ad ad, AdError error) {
                Log.d(TAG, "Error fetching ad placementId: "+ad.getPlacementId()+" Error: "+ error.getErrorMessage());
                Log.d(TAG, "Error code: "+error.getErrorCode());
                Log.d(TAG, "Error: "+error);

            }

            @Override
            public void onAdLoaded(Ad ad) {

                if (ad != nativeAd) {
                    return;
                }

                if (nativeAd != null) {
                    nativeAd.unregisterView();
                }

                // Set the Text.
                nativeAdTitle.setText(nativeAd.getAdTitle());
                nativeAdSocialContext.setText(nativeAd.getAdSocialContext());
                nativeAdBody.setText(nativeAd.getAdBody());
                nativeAdCallToAction.setText(nativeAd.getAdCallToAction());

                // Download and display the ad icon.
                NativeAd.Image adIcon = nativeAd.getAdIcon();
                NativeAd.downloadAndDisplayImage(adIcon, nativeAdIcon);

                // Download and display the cover image.
                nativeAdMedia.setNativeAd(nativeAd);

                // Add the AdChoices icon
                LinearLayout adChoicesContainer = (LinearLayout) findViewById(R.id.ad_choices_container);
                AdChoicesView adChoicesView = new AdChoicesView(MainActivity.this, nativeAd, true);
                adChoicesContainer.addView(adChoicesView);

                // Register the Title and CTA button to listen for clicks.
                List<View> clickableViews = new ArrayList<>();
                clickableViews.add(nativeAdTitle);
                clickableViews.add(nativeAdCallToAction);
                nativeAd.registerViewForInteraction(ads_CardView,clickableViews);


                //Show the ad card view, as the ads are loaded
                ads_CardView.setVisibility(View.VISIBLE);
                YoYo.with(Techniques.FadeIn)
                        .duration(2000)
                        .playOn(ads_CardView);

                Log.d(TAG, "Ad Loaded");
            }

            @Override
            public void onAdClicked(Ad ad) {
                Log.d(TAG, "Ad Clicked");
            }

            @Override
            public void onLoggingImpression(Ad ad) {
                Log.d(TAG, "Ad Impression Logged");
            }
        });

        // Request an ad
        nativeAd.loadAd(NativeAd.MediaCacheFlag.ALL);
    }



    @Override
    public void onReceiveAd(View banner) {
        Log.d(TAG, "StartApp Ads Received");
        this.banner.showBanner();

        banner.setVisibility(View.VISIBLE);
        YoYo.with(Techniques.FadeIn)
                .duration(2000)
                .playOn(banner);
    }
    @Override
    public void onFailedToReceiveAd(View banner) {
        Log.d(TAG, "Ads Receive Failed");
    }
    @Override
    public void onClick(View banner) {
        Log.d(TAG, "Ads Clicked");
    }

}
