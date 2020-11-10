package com.certifyglobal.utils;

import android.content.Context;
import android.util.Base64;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

import static java.nio.charset.StandardCharsets.UTF_8;

public class RSAKeypair {

    public static void getRSAPublic(Context context) {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
            String publicBaseStr = Base64.encodeToString(new X509EncodedKeySpec(publicKey.getEncoded()).getEncoded(), Base64.DEFAULT);
            RSAPrivateCrtKey privateKey2 = (RSAPrivateCrtKey) keyPair.getPrivate();
            String privateKeyBaseStr = Base64.encodeToString(privateKey2.getEncoded(), Base64.DEFAULT);
            Utils.saveToPreferences(context, PreferencesKeys.publicKey, publicBaseStr);
            Utils.saveToPreferences(context, PreferencesKeys.privateKey, privateKeyBaseStr);
        } catch (Exception e) {
            Logger.error("getRSAPublic(Context context)", e.getMessage());
        }

    }

    public static String DecryptRSA(String privateBase64, String encryptedStr) {
        //get private key
        try {
            byte[] decodedPkcs8 = Base64.decode(privateBase64, Base64.DEFAULT);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decodedPkcs8);
            PrivateKey privateKey = KeyFactory.getInstance("RSA").generatePrivate(keySpec);
            //create cipher in decrypt mode
            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding");//256
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            //get the encrypted base64 string
            byte[] encrypted = Base64.decode(encryptedStr, Base64.DEFAULT);
            byte[] decrypted = cipher.doFinal(encrypted);
            return new String(decrypted);
        } catch (Exception e) {
            Logger.error("DecryptRSA(String privateBase64, String encryptedStr)", e.getMessage());
            return "";
        }
    }

    public static String signData(String uuId, String requestId, String privateKeyStr) {
        try {
            String plainText = String.format("%s%s", uuId, requestId);
            byte[] decodedPkcs8 = Base64.decode(privateKeyStr, Base64.DEFAULT);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decodedPkcs8);
            PrivateKey privateKey = KeyFactory.getInstance("RSA").generatePrivate(keySpec);
            Signature privateSignature = Signature.getInstance("SHA256withRSA");
            privateSignature.initSign(privateKey);
            privateSignature.update(plainText.getBytes(UTF_8));
            byte[] signature = privateSignature.sign();
            return Base64.encodeToString(signature, Base64.DEFAULT);
        } catch (Exception e) {
            Logger.error("signData(String uuId, String requestId, String privateKeyStr)", e.getMessage());
            return "";
        }
    }
}
