/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.util;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;

//import sun.misc.BASE64Decoder;
//import sun.misc.BASE64Encoder;

/**
 *
 * @author conghv
 */
public class PasswordEncoderQltn {

    private static final char[] PASSWORD = "d4ebaae0-e44f-11e2-a28f-0800200c9a66".toCharArray();
    private static final byte[] SALT = {
        (byte) 0xba, (byte) 0x96, (byte) 0x57, (byte) 0x14,
        (byte) 0xef, (byte) 0xd4, (byte) 0x20, (byte) 0xf1};

    public static String encrypt(String property) throws GeneralSecurityException, UnsupportedEncodingException {
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
        SecretKey key = keyFactory.generateSecret(new PBEKeySpec(PASSWORD));
        Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
        pbeCipher.init(Cipher.ENCRYPT_MODE, key, new PBEParameterSpec(SALT, 20));
        return base64Encode(pbeCipher.doFinal(property.getBytes("UTF-8")));
    }

    public static String encryptUrlParam(String property) throws GeneralSecurityException, UnsupportedEncodingException {
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
        SecretKey key = keyFactory.generateSecret(new PBEKeySpec(PASSWORD));
        Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
        pbeCipher.init(Cipher.ENCRYPT_MODE, key, new PBEParameterSpec(SALT, 20));
        String res = base64Encode(pbeCipher.doFinal(property.getBytes("UTF-8")));
        res = res.replace('+', '-').replace('/', '_').replace("%", "%25").replace("\n", "%0A");
        return res;
    }

    private static String base64Encode(byte[] bytes) {
        byte[] encodedBytes = Base64.encodeBase64(bytes);
        return new String(encodedBytes);


//        return new BASE64Encoder().encode(bytes);
    }

    public static String decrypt(String property) throws GeneralSecurityException, IOException {
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
        SecretKey key = keyFactory.generateSecret(new PBEKeySpec(PASSWORD));
        Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
        pbeCipher.init(Cipher.DECRYPT_MODE, key, new PBEParameterSpec(SALT, 20));
        return new String(pbeCipher.doFinal(base64Decode(property)), "UTF-8");
    }

    public static String decryptUrlParam(String property) throws GeneralSecurityException, IOException {
        property = property.replace("%0A", "\n").replace("%25", "%").replace('_', '/').replace('-', '+');
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
        SecretKey key = keyFactory.generateSecret(new PBEKeySpec(PASSWORD));
        Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
        pbeCipher.init(Cipher.DECRYPT_MODE, key, new PBEParameterSpec(SALT, 20));
        return new String(pbeCipher.doFinal(base64Decode(property)), "UTF-8");
    }

    private static byte[] base64Decode(String property) throws IOException {
        byte[] decodedBytes = Base64.decodeBase64(property);
        return decodedBytes;

//        return new BASE64Decoder().decodeBuffer(property);
    }
}
