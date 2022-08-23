package id.co.sistema.vkey;

import static com.vkey.android.vguard.VGuardBroadcastReceiver.ACTION_FINISH;
import static com.vkey.android.vguard.VGuardBroadcastReceiver.ACTION_SCAN_COMPLETE;
import static com.vkey.android.vguard.VGuardBroadcastReceiver.VGUARD_STATUS;
import static com.vkey.android.vguard.VGuardBroadcastReceiver.VOS_READY;
import static id.co.sistema.vkey.Constant.PROFILE_LOADED;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.common.eventbus.EventBus;
import com.vkey.android.vguard.ActivityLifecycleHook;
import com.vkey.android.vguard.LocalBroadcastManager;
import com.vkey.android.vguard.VGExceptionHandler;
import com.vkey.android.vguard.VGuard;
import com.vkey.android.vguard.VGuardBroadcastReceiver;
import com.vkey.android.vguard.VGuardFactory;
import com.vkey.android.vguard.VGuardLifecycleHook;

import java.io.InputStream;

import vkey.android.vos.Vos;
import vkey.android.vos.VosWrapper;

public class Customvkey extends Application implements VGExceptionHandler , Application.ActivityLifecycleCallbacks {

    private VGuard vGuardMgr;
    private VGuardLifecycleHook hook;
    private VGuardBroadcastReceiver broadcastRvcr;
    private Vos mVos;
    private VGuardBroadcastReceiver swButtonRcvr;
    private VGuardBroadcastReceiver resetVOSTrustedStorageReceiver;



    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {

    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {

    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {

    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {

    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {

    }

    @Override
    public void handleException(Exception e) {

    }

    private void startVos(Context ctx){
        mVos = new Vos(ctx);
        mVos.registerVosWrapperCallback((VosWrapper.Callback) this);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    InputStream is = ctx.getAssets().open("kernel.bin");
                    byte[] kernelData = new byte[is.available()];
                    is.read(kernelData);
                    is.close();
                    long vosReturnCode = mVos.start(kernelData ,null , null , null , null);
                    if (vosReturnCode > 0){
                        //successfully start
                        VosWrapper vosWrapper = VosWrapper.getInstance(ctx);
                        String version = vosWrapper.getProcessorVersion();
                    }else {
                        //failed to start vos , handle error
                    }
                } catch (Exception e) {
                    Log.d("ulol" , "gagal");
                }

            }

        });
    }



}
