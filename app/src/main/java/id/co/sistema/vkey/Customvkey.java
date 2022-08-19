package id.co.sistema.vkey;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.vkey.android.vguard.VGExceptionHandler;
import com.vkey.android.vguard.VGuard;
import com.vkey.android.vguard.VGuardBroadcastReceiver;
import com.vkey.android.vguard.VGuardLifecycleHook;

import vkey.android.vos.Vos;

public class Customvkey extends Application implements VGExceptionHandler , Application.ActivityLifecycleCallbacks {

    private VGuard vGuardMgr;
    private VGuardLifecycleHook hook;
    private VGuardBroadcastReceiver broadcastRvcr;
    private Vos mVos;

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


}
