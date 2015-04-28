package com.jcryptosync.utils;

import com.jcryptosync.PrimaryKey;
import com.jcryptosync.exceptoins.NoCorrectPasswordException;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PrimaryKeyUtils {

    public static PrimaryKey generateNewPrimaryKey() {
        KeyGenerator keyGen = null;

        try {
            keyGen = KeyGenerator.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        keyGen.init(128);
        SecretKey secretKey = keyGen.generateKey();

        return PrimaryKey.fromSecretKey(secretKey);
    }

    public static SecretKey generateKeyFromPassword(String password) {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        byte[] hash = null;

        try {
            hash = digest.digest(password.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return new SecretKeySpec(hash, 0, 16, "AES");
    }

    public static byte[] encryptKey(PrimaryKey primaryKey, SecretKey key) {
        Cipher cipher = null;

        try {
            cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }
        try {
            cipher.init(Cipher.ENCRYPT_MODE, key);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }

        byte[] plainKey = new byte[0];
        try {
            plainKey = primaryKey.toJson().getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        byte[] cipherKey = null;

        try {
            cipherKey = cipher.doFinal(plainKey);
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }

        return cipherKey;
    }

    public static PrimaryKey decryptKey(byte[] cryptKey, SecretKey key) throws NoCorrectPasswordException {
        Cipher cipher = null;

        try {
            cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }
        try {
            cipher.init(Cipher.DECRYPT_MODE, key);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }

        byte[] plainKey = null;

        try {
            plainKey = cipher.doFinal(cryptKey);
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            throw new NoCorrectPasswordException("Неправильный пароль");
        }

        String jsonKey = null;
        try {
            jsonKey = new String(plainKey, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return PrimaryKey.fromJson(jsonKey);

    }
}
