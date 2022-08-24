package id.co.sistema.vkey;



import static com.vkey.android.vguard.VGuardBroadcastReceiver.ACTION_FINISH;
import static com.vkey.android.vguard.VGuardBroadcastReceiver.ACTION_SCAN_COMPLETE;
import static com.vkey.android.vguard.VGuardBroadcastReceiver.VGUARD_STATUS;
import static com.vkey.android.vguard.VGuardBroadcastReceiver.VOS_READY;
import vkey.android.vos.Vos;
import vkey.android.vos.VosWrapper;


import com.vkey.android.vguard.MemoryConfiguration;
import com.vkey.android.vguard.VGException;
import com.vkey.securefileio.SecureFileIO;
import com.vkey.vos.signer.taInterface;

import static id.co.sistema.vkey.Constant.PROFILE_LOADED;
import static id.co.sistema.vkey.Constant.VOS_FIRMWARE_RETURN_CODE_KEY;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.vkey.android.vguard.ActivityLifecycleHook;
import com.vkey.android.vguard.VGuardLifecycleHook;
import com.vkey.android.vguard.VGuard;
import com.vkey.android.vguard.VGuardFactory;
import com.vkey.android.vguard.VGExceptionHandler;
import com.vkey.android.vguard.LocalBroadcastManager;
import com.vkey.android.vguard.VGuardBroadcastReceiver;

import java.io.File;
import java.nio.file.Files;


import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity  implements VGExceptionHandler, VosWrapper.Callback {

    private VGuard vGuardMgr;
    private VGuardLifecycleHook hook;
    private VGuardBroadcastReceiver broadcastRvcr;
    private Vos mVos;
    private TextView tv;
    private taInterface CryptoTA = taInterface.getInstance();





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        tv = findViewById(R.id.tv);
//        mVos = new Vos(this);
//        mVos.registerVosWrapperCallback((VosWrapper.Callback) this);


//        VosWrapper iVosWrapper = VosWrapper.getInstance(this);
//        CryptoTA.loadTA();
//        CryptoTA.initialize();
//        CryptoTA.processManifest(this);
//        iVosWrapper.setTrustedTimeServerUrl("https://domain.com/vtap/time");
//        CryptoTA.unloadTA();
//        iVosWrapper.stopVOS();

        setupVguard();
        setupVguarda();
        startVos(this);
        encryptDecrypt(this);
        tv.setText(vGuardMgr.getTroubleshootingId());
        encryptBlockDataFile(this);
        encryptBlockData();
        encryptDecExistingFile(this);
        getReturnCode();
        Log.d("rede" , "aa:"+ vGuardMgr.getIsVosStarted());
    }



    private void encryptDecExistingFile(Context context){
        String filePath = context.getFilesDir().getAbsolutePath() + "/nonEncryptedFile.txt";
        try {
            com.vkey.securefileio.SecureFileIO.encryptFile(filePath , "");
            byte[] decrypt = SecureFileIO.decryptFile(filePath ,"");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private void encryptBlockDataFile(Context context)  {
        byte[] input = "Quick brown fox jumps over the lazy dog. 1234567890 some_one@somewhere.com".getBytes();
        String encrypteFile = context.getFilesDir().getAbsolutePath() + "/encryptedFile.txt";

        try {
            com.vkey.securefileio.SecureFileIO.encryptData(input , encrypteFile , "" , false);
            byte[] decrypted = com.vkey.securefileio.SecureFileIO.decryptFile(encrypteFile , "");

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void encryptBlockData() {
        byte[] input = "etslsmfks skdmfksmdf ksdmfksmdkf".getBytes();
        try {
            byte[] cipher = com.vkey.securefileio.SecureFileIO.encryptData(input);
            byte[] decrypt = com.vkey.securefileio.SecureFileIO.decryptData(cipher);
            Log.d("yoyo", "encryptDecrypt: "+decrypt);

        } catch (IOException e) {
            e.printStackTrace();
            Log.d("yoyo", "encryptDecrypt: "+e);

        }
    }


    private void encryptDecrypt(Context context){
        //Encrypting/Decrypting a String to/from a File
        String test = "sasdasdasd 837483743 asdasda@.comsd";
        String encryptFile = context.getFilesDir().getAbsolutePath() + "encryptedFile.txt";
//        if(!encryptFile.exists()) encryptFile.createNewFile();

        try {
            com.vkey.securefileio.SecureFileIO.encryptString(test ,encryptFile, "P@ssw0rd" , false);
            String decrypt = com.vkey.securefileio.SecureFileIO.decryptString(encryptFile , "P@ssw0rd");
            Log.d("TESTT", "encryptDecrypt: "+decrypt);
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("TESTT", "encryptDecrypt: "+e);
        }

    }

    @Override
    public void handleException(Exception e) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (vGuardMgr != null){
            vGuardMgr.onResume(hook);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (vGuardMgr != null){
            vGuardMgr.onPause(hook);
        }
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastRvcr);
        if (vGuardMgr != null){
            vGuardMgr.destroy();
        }
        super.onDestroy();
    }

    private void setupVguard() {


        broadcastRvcr = new VGuardBroadcastReceiver(this){
            @Override
            public void onReceive(Context context, Intent intent) {
                super.onReceive(context, intent);
                   if (PROFILE_LOADED.equals(intent.getAction())){}
                   if (VGUARD_STATUS.equals(intent.getAction())){}
                   if (ACTION_SCAN_COMPLETE.equals(intent.getAction())){
                    }
                   if (VOS_READY.equals(intent.getAction())){
                       Toast.makeText(MainActivity.this , "Done" , Toast.LENGTH_LONG).show();
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
            vGuardMgr.setVGExceptionHandler(this);
            hook = new ActivityLifecycleHook(vGuardMgr);
            Log.d("coba" , "jalan");
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("exxx" , "dd" + e);
        }



    }

    private void setupVguarda() {
        try {
            new VGuardFactory().getVGuard(this , new VGuardFactory.Builder()
            .setDebugable(true)
            .setAllowsArbitraryNetworking(false)
            .setMemoryConfiguration(MemoryConfiguration.DEFAULT)
            .setVGExceptionHandler(this));
            Log.d("ppp", "setupVguard: sss");

        } catch (Exception e) {
            e.printStackTrace();
            Log.d("ppp", "setupVguard: failed ");
        }
    }
    //    VKeySecureKeypad.VKSecureKeyboardLayout =

//    private void stopMvos(){
//        mVos.stop();
//    }
private void startVos(Context ctx){
 mVos = new Vos(ctx);
 mVos.registerVosWrapperCallback(this);
    new Thread(new Runnable() {
        @Override
        public void run() {
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
//                            Log.d("testingg" , "berhasil");
                        }else {
                            //failed to start vos , handle error
                            Log.d("failedd", "run: gaal");
                        }
                    } catch (IOException e) {
                        Log.d("yyy" , "gagal" + e);
                    }
                }

        });
    }

    private long getReturnCode(){
        Intent intent = new Intent();
        long firmwareReturnCode = intent.getLongExtra(VOS_FIRMWARE_RETURN_CODE_KEY, 0);
        Log.d("fwcode", "getReturnCode: "+firmwareReturnCode);
        return firmwareReturnCode;
    }

    @Override
    public boolean onNotified(int i, int i1) {
        return false;
    }
}



