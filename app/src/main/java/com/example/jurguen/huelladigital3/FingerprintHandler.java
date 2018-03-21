package com.example.jurguen.huelladigital3;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.jar.Manifest;

/**
 * Created by Jurguen on 28/04/2017.
 */

@RequiresApi(api = Build.VERSION_CODES.M)

//Hace el llamado, es decir valida que la huella digital exista
class FingerprintHandler extends FingerprintManager.AuthenticationCallback{
    private Context context;

    public FingerprintHandler(Context context)
    {
        this.context= context;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    //
    public void startAuthentication(FingerprintManager fingerprintManager, FingerprintManager.CryptoObject cryptoObject) {
        CancellationSignal cenCancellationSignal= new CancellationSignal();
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.USE_FINGERPRINT)!= PackageManager.PERMISSION_GRANTED){
            return;
        }
        fingerprintManager.authenticate(cryptoObject,cenCancellationSignal,0,this,null);
    }

    @Override
    //Si la autentificacion falla
    public void onAuthenticationFailed(){
        super.onAuthenticationFailed();
        Toast.makeText(context,"Fallo",Toast.LENGTH_SHORT).show();
    }

    @Override
    //si la huella es correcta entonces ingresa
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result){
        super.onAuthenticationSucceeded(result);
        context.startActivity(new Intent(context,HomeActivity.class));
    }
}
