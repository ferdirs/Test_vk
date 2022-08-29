package id.co.sistema.vkey;

import static com.vkey.android.vguard.VGuardBroadcastReceiver.ACTION_FINISH;
import static com.vkey.android.vguard.VGuardBroadcastReceiver.ACTION_SCAN_COMPLETE;
import static com.vkey.android.vguard.VGuardBroadcastReceiver.VGUARD_STATUS;
import static com.vkey.android.vguard.VGuardBroadcastReceiver.VOS_READY;
import static id.co.sistema.vkey.Constant.PROFILE_LOADED;
import static id.co.sistema.vkey.Constant.VOS_FIRMWARE_RETURN_CODE_KEY;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.vkey.android.internal.vguard.engine.BasicThreatInfo;
import com.vkey.android.vguard.ActivityLifecycleHook;
import com.vkey.android.vguard.LocalBroadcastManager;
import com.vkey.android.vguard.MemoryConfiguration;
import com.vkey.android.vguard.VGExceptionHandler;
import com.vkey.android.vguard.VGuard;
import com.vkey.android.vguard.VGuardBroadcastReceiver;
import com.vkey.android.vguard.VGuardFactory;
import com.vkey.android.vguard.VGuardLifecycleHook;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import vkey.android.vos.Vos;
import vkey.android.vos.VosWrapper;

public class SplashScreen extends AppCompatActivity implements VGExceptionHandler {

    private VGuard vGuardMgr;
    private VGuardLifecycleHook hook;
    private VGuardBroadcastReceiver broadcastRvcr;
    private Vos mVos;
    private TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        getSupportActionBar().hide();

        Handler handler = new Handler();
        handler.postDelayed(() -> {
            Intent intent = new Intent(SplashScreen.this , MainActivity.class);
            startActivity(intent);
//                setupVguard();
//                startVos(getApplicationContext());
            finish();
        },5000L);
    }



    private void startVos(Context ctx){
        mVos = new Vos(ctx);
        mVos.registerVosWrapperCallback((VosWrapper.Callback) getApplicationContext());
        new Thread(() -> {
            try { InputStream is = ctx.getAssets().open("firmware");
                byte[] kernelData = new byte[is.available()];
                is.read(kernelData);
                is.close();
                long vosReturnCode = mVos.start(kernelData ,null , null , null , null);
                Log.d("testingg" , "berhasil");
                if (vosReturnCode >= 0){
                    //successfully start
                    VosWrapper vosWrapper = VosWrapper.getInstance(ctx);
                    String version = vosWrapper.getProcessorVersion();
                    Log.d("testingg" , "berhasil");
                }else {
                    //failed to start vos , handle error
                    Log.d("failedd", "run: gaal");
                }
            } catch (IOException e) {
                Log.d("yyy" , "gagal" + e);
            }
        });
    }

    private void setupVguard() {

        broadcastRvcr = new VGuardBroadcastReceiver(this){
            @Override
            public void onReceive(Context context, Intent intent) {
                super.onReceive(context, intent);


                if (PROFILE_LOADED.equals(intent.getAction())){
                    Log.d("redee" , "aa:" + vGuardMgr.getIsVosStarted());
                }
                if (VGUARD_STATUS.equals(intent.getAction())){
                    if (intent.hasExtra(Constant.VGUARD_INIT_STATUS)){
                        boolean initStatus = intent.getBooleanExtra(Constant.VGUARD_INIT_STATUS , false);
                        String msg = "\n " + VGUARD_STATUS + ":" + initStatus;
                        Log.d("jason", msg);
                        if (!initStatus){
                            try {
                                JSONObject jsonObject = new JSONObject(intent.getStringExtra(VGUARD_MESSAGE));
                                Log.d("jason", jsonObject.getString("code"));
                                Log.d("jason", "Description");
                                msg += " " + jsonObject.toString();
                            }catch (Exception e) {

                            }
                        }
                    }

                    if (intent.hasExtra(VGUARD_HANDLE_THREAT_POLICY)) {
                        ArrayList<Parcelable> detectedThreats =
                                (ArrayList<Parcelable>)
                                        intent.getParcelableArrayListExtra(VGuardBroadcastReceiver.SCAN_COMPLETE_RESULT);
                        StringBuilder builder = new StringBuilder();
                        for (Parcelable info : detectedThreats) {
                            String infoStr = ((BasicThreatInfo) info).toString();
                            builder.append(infoStr).append("\n");
                        }
                        int highestResponse =
                                intent.getIntExtra(VGUARD_HIGHEST_THREAT_POLICY, -1);
                        String alertTitle =
                                intent.getStringExtra(VGUARD_ALERT_TITLE);
                        String alertMsg =
                                intent.getStringExtra(VGUARD_ALERT_MESSAGE);
                        long disabledAppExpired =
                                intent.getLongExtra(VGUARD_DISABLED_APP_EXPIRED, 0);
                        if (highestResponse > 0) {
                            builder.append("highest policy: " +
                                    highestResponse).append("\n");
                        }
                        if (!TextUtils.isEmpty(alertTitle)) {
                            builder.append("alertTitle: " + alertTitle).append("\n");
                        }
                        if (!TextUtils.isEmpty(alertMsg)) {
                            builder.append("alertMsg: " + alertMsg).append("\n");
                        }
                        if (disabledAppExpired > 0) {
                            SimpleDateFormat format = new SimpleDateFormat("yyyy-MMdd HH:mm:ss");
                            String activeDate = format.format(new
                                    Date(disabledAppExpired));
                            builder.append("App can use again after: " +
                                    activeDate).append("\n");
                        }
                    }
                }
                if (ACTION_SCAN_COMPLETE.equals(intent.getAction())){
                    ArrayList<Parcelable> detectedThread = (ArrayList<Parcelable>)
                            intent.getParcelableArrayListExtra(SCAN_COMPLETE_RESULT);
                    for (Parcelable info : detectedThread){
                        String infoStr = ((BasicThreatInfo)info).toString();
                    }
                }
                if (VOS_READY.equals(intent.getAction())){
                    Toast.makeText(getApplicationContext() , "Done" , Toast.LENGTH_LONG).show();
                    long firmwareRetuenCode = getReturnCode();
                    if (firmwareRetuenCode >= 0 ){
                        Log.d("redd", "onReceive: ");
                        if (vGuardMgr == null){
                            Log.d("redd", "onReceive: o");
                            try {
                                vGuardMgr = VGuardFactory.getInstance();
                                hook = new ActivityLifecycleHook(vGuardMgr);
                                Log.d("redd", "onReceive: ");
                            }catch (Exception e){
                                Log.d("redd", "onReceive: "+e);
                            }
                        }
                    }
                }
            }
        };



        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.registerReceiver(broadcastRvcr , new IntentFilter(PROFILE_LOADED));
        localBroadcastManager.registerReceiver(broadcastRvcr , new IntentFilter(VGUARD_STATUS));
        localBroadcastManager.registerReceiver(broadcastRvcr , new IntentFilter(ACTION_SCAN_COMPLETE));
        localBroadcastManager.registerReceiver(broadcastRvcr , new IntentFilter(VOS_READY));
        localBroadcastManager.registerReceiver(broadcastRvcr , new IntentFilter(ACTION_FINISH));
        try {
            VGuardFactory.debug = true;
            //deprecated
            vGuardMgr= new VGuardFactory().getVGuard(this );
            vGuardMgr.setVGExceptionHandler((VGExceptionHandler) this);
            hook = new ActivityLifecycleHook(vGuardMgr);
            Log.d("coba" , "jalan");
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("exxx" , "dd" + e);
        }



    }
    private long getReturnCode(){
        Intent intent = new Intent();
        long firmwareReturnCode = intent.getLongExtra(VOS_FIRMWARE_RETURN_CODE_KEY, 0);
        Log.d("fwcode", "getReturnCode: "+firmwareReturnCode);
        return firmwareReturnCode;
    }

    @Override
    public void handleException(Exception e) {

    }
}