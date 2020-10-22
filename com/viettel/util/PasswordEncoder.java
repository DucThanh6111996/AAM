/*
 * Created on Nov 14, 2013
 *
 * Copyright (C) 2013 by Viettel Network Company. All rights reserved
 */
package com.viettel.util;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import org.apache.commons.codec.binary.Base64;

/**
 * Tiện ích thao tác với password.
 * Dùng cho việc encode và decode password theo Base64.
 * 
 *
 * @author Nguyen Hai Ha (hanh45@viettel.com.vn)
 * @since Nov 15, 2013
 * @version 1.0.0
 */
public class PasswordEncoder {

    private static final char[] PASSWORD = "cdbaenflzXKmHgdsfwca@#$faraafasjweoirfn,sdfkjsahfsdahfksadfkj4817hsakjfgwfhskjdfgsajfgwiuerjw!^*&!^#*!*&#^*&#^!&*!#al,xvnmnasfjsafaksfhowierhweuigfsdjvbsdvsafj%^%$^%#!#&^!%#*&!(#(*!*#)(!!#&(!*#!^##!#%asfsadbfjsahkhgkshuwergfsafsbfhsdf^%*&^&*@&%#&^@%#*!*(*@^!*snkzmnabvdujhagrqwrhalnadsgbnlxxxsngdlksdsgm".toCharArray();
    private static final byte[] SALT = {
        (byte) 0xde, (byte) 0x13, (byte) 0x30, (byte) 0x12,
        (byte) 0xdf, (byte) 0x33, (byte) 0x21, (byte) 0x32,
    };
    
    /**
     * Hàm encode password.
     */
    public static String encrypt(String property) throws GeneralSecurityException, UnsupportedEncodingException {
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
        SecretKey key = keyFactory.generateSecret(new PBEKeySpec(PASSWORD));
        Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
        pbeCipher.init(Cipher.ENCRYPT_MODE, key, new PBEParameterSpec(SALT, 20));
        return base64Encode(pbeCipher.doFinal(property.getBytes("UTF-8")));
    }

    private static String base64Encode(byte[] bytes) {
        return Base64.encodeBase64String(bytes);
    }

    /**
     * Hàm decode password
     */
    public static String decrypt(String property) throws GeneralSecurityException, IOException {
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
        SecretKey key = keyFactory.generateSecret(new PBEKeySpec(PASSWORD));
        Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
        pbeCipher.init(Cipher.DECRYPT_MODE, key, new PBEParameterSpec(SALT, 20));
        return new String(pbeCipher.doFinal(base64Decode(property)), "UTF-8");
    }

    private static byte[] base64Decode(String property) throws IOException {
        return Base64.decodeBase64(property);
    }

}
