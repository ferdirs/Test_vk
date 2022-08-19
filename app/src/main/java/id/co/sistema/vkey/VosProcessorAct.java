package id.co.sistema.vkey;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;

import com.vkey.android.vguard.VGException;

import java.io.IOException;
import java.io.InputStream;

import vkey.android.vos.Vos;
import vkey.android.vos.VosWrapper;

public class VosProcessorAct extends AppCompatActivity {



    private Vos mVos;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vos_processor);

        mVos.registerVosWrapperCallback((VosWrapper.Callback) this);
        startVos(this);

    }

    private void stopVos(){
        mVos.stop();
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
                } catch (IOException e) {

                }

            }

        });
    }
}