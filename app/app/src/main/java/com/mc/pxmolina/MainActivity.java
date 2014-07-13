package com.mc.pxmolina;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.image.SmartImageView;

import java.io.UnsupportedEncodingException;
import android.support.v7.widget.ShareActionProvider;


public class MainActivity extends ActionBarActivity {

    private String imageUrl = "http://www.confidencial.com.ni/img/cartoons/364.jpg";

    /** The view to show the ad. */
    private AdView adView;
    private AdRequest adRequest;
    private String deviceid;

    /* Your ad unit id. Replace with your actual ad unit id. */
    private static final String AD_UNIT_ID = "ca-app-pub-2015513932539714/6571004228";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Needs to be called before setting the content view
        supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_main);

        init();
        adMobInit();
    }

    private void adMobInit() {

        // Create an ad.
        adView = new AdView(this);
        adView.setAdSize(AdSize.SMART_BANNER);
        adView.setAdUnitId(AD_UNIT_ID);

        // Add the AdView to the view hierarchy. The view will have no size
        // until the ad is loaded.
        LinearLayout layout = (LinearLayout) findViewById(R.id.ad);
        layout.removeAllViews();

        if (isConnected()) {
            layout.setVisibility(View.VISIBLE);
            layout.addView(adView);

            final TelephonyManager tm = (TelephonyManager) getBaseContext()
                    .getSystemService(Context.TELEPHONY_SERVICE);

            deviceid = tm.getDeviceId();

            // Create an ad request. Check logcat output for the hashed device
            // ID to
            // get test ads on a physical device.
            adRequest = new AdRequest.Builder()
                    .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                    .addTestDevice(deviceid).build();

            // Start loading the ad in the background.
            adView.loadAd(adRequest);
        } else {
            layout.setVisibility(View.GONE);
        }

    }

    private boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = false;
        if (activeNetwork != null)
            isConnected = true;
        return isConnected;
    }

    private void init(){

        if( isConnected() ){

            final SmartImageView myImage = (SmartImageView) this.findViewById(R.id.image);


            AsyncHttpClient client = new AsyncHttpClient();
            client.get("http://pxmolina.herokuapp.com/", new AsyncHttpResponseHandler() {

                @Override
                public void onStart() {
                    setSupportProgressBarIndeterminateVisibility(true);
                }

                @Override
                public void onSuccess(int statusCode, org.apache.http.Header[] headers, byte[] response) {
                    try{
                        imageUrl = new String(response, "UTF-8");
                        myImage.setImageUrl(imageUrl);
                        supportInvalidateOptionsMenu();
                        setSupportProgressBarIndeterminateVisibility(false);
                    }catch (UnsupportedEncodingException e){
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }

                }

                @Override
                public void onFailure(int statusCode, org.apache.http.Header[] headers, byte[] errorResponse, Throwable e) {
                    Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
                    setSupportProgressBarIndeterminateVisibility(false);
                }
            });

        }
        else{
            Toast.makeText(getApplicationContext(),getString(R.string.connection),Toast.LENGTH_LONG).show();
        }


    }

    private Intent createShareIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getResources().getString(R.string.app_name));
        shareIntent.putExtra(Intent.EXTRA_TEXT, imageUrl);
        return shareIntent;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem shareOpt = menu.findItem(R.id.action_share);
        ShareActionProvider myShareActionProvider = (ShareActionProvider)MenuItemCompat.getActionProvider(shareOpt);
        myShareActionProvider.setShareIntent( createShareIntent() );

        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //if (id == R.id.action_settings) {
        //    return true;
        //}
        return super.onOptionsItemSelected(item);
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        adMobInit();

    }

    @Override
    public void onResume() {
        super.onResume();
        if (adView != null) {
            adView.resume();
        }
    }

    @Override
    public void onPause() {
        if (adView != null) {
            adView.pause();
        }
        super.onPause();
    }

    /** Called before the activity is destroyed. */
    @Override
    public void onDestroy() {
        // Destroy the AdView.
        if (adView != null) {
            adView.destroy();
        }
        super.onDestroy();
    }
}
