package com.example.jurguen.huelladigital3;

import android.Manifest;
import android.app.KeyguardManager;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;


public class MainActivity extends AppCompatActivity {

    private KeyStore keyStore;
    private static final String KEY_NAME = "EDMTDev";
    private Cipher cipher;
    private TextView textView;

    @RequiresApi(api = Build.VERSION_CODES.M)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*Obtiene los servicios para desbloquear el celular con la huella digital
        La clase Keyguardmanager me permite bloquear o desbloquear entrada.*/
        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        FingerprintManager fingerprintManager = (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);

        //Verifica si los permisos de obtenidos de fingerprint en el manifest son iguales que los permisos en el dispositivo
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        //si el celular no permite detectar la huella digital envia el mensaje de error
        //esto se realiza con la funcion .isHardwareDetected.
        if (!fingerprintManager.isHardwareDetected()) {
            Toast.makeText(this, "autentificacion de huella no habilitada", Toast.LENGTH_LONG).show();
        }
        else {
            /*La huella no ha sido registrada. hasEnrolledFingerprints() esta funcion me permite verificar que el
            dispositivo tenga registrada una huella digital.*/
            if (!fingerprintManager.hasEnrolledFingerprints()) {
                Toast.makeText(this, "Registre una huella dactilar en configuraciones", Toast.LENGTH_SHORT).show();
            }
            else {
                /*devuelve si el bloqueo requiere alguna contraseña, este metodo permite ver si el bloqueo de pantalla
                es seguro es decir si hay una contraseña ya establecida*/
                if (!keyguardManager.isKeyguardSecure()) {
                    Toast.makeText(this, "Pantalla de bloqueo no configurada", Toast.LENGTH_SHORT).show();
                }
                //genera llave
                else {
                    genKey();
                }
                //Llave encriptada
                if (cipherInit()) {
                    //para generar un objeto incriptado
                    FingerprintManager.CryptoObject cryptoObject = new FingerprintManager.CryptoObject(cipher);
                    FingerprintHandler helper = new FingerprintHandler(this);
                    helper.startAuthentication(fingerprintManager, cryptoObject);
                }
            }
        }
    }

    private boolean cipherInit() {
        try {
            // 1 PKCS7 (Para hacer firmas digitales)
            // 2 AES (Advanced Encryption Standard)
            // 3 CBC  (Criminal Background check)
            // clase Cipher.getInstance que permite realizar un cifrado a partir de AES,CBC,PKCS7
            cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }
        try {

            keyStore.load(null);
            //obtiene la llave generada
            SecretKey key = (SecretKey) keyStore.getKey(KEY_NAME, null);
            //crea la llave encriptada
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return true;
        } catch (IOException e1) {
            e1.printStackTrace();
            return false;
        } catch (NoSuchAlgorithmException e1) {
            e1.printStackTrace();
            return false;
        } catch (CertificateException e1) {
            e1.printStackTrace();
            return false;
        } catch (UnrecoverableKeyException e1) {
            e1.printStackTrace();
            return false;
        } catch (KeyStoreException e1) {
            e1.printStackTrace();
            return false;
        } catch (InvalidKeyException e1) {
            e1.printStackTrace();
            return false;
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void genKey() {
        try {
            //obtiene la llave
            keyStore=keyStore.getInstance("AndroidKeyStore");
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
        KeyGenerator keyGenerator = null;
        try {
            //Esta clase proporciona la funcionalidad de un generador de claves secreto (simétrico).
            //Los generadores de claves se construyen utilizando uno de los métodos de clase getInstance de esta clase.
            // se seleciona el tipo de seguridad de "(Advanced Encryption Standard)"
            keyGenerator=KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES,"AndroidKeyStore");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }
        try {
            keyStore.load(null);
            keyGenerator.init(new KeyGenParameterSpec.Builder(KEY_NAME,KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                     .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7).build());
            keyGenerator.generateKey();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
    }
}
