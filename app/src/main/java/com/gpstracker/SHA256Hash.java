package com.gpstracker;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by RGarai on 14.10.2016.
 */

public class SHA256Hash {

    public static String sha256(String s) { try {

        // Create MD5 Hash
        MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
        digest.update(s.getBytes());
        byte messageDigest[] = digest.digest();

        // Create Hex String
        StringBuffer hexString = new StringBuffer();
        for (int i=0; i<messageDigest.length; i++)
            hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
        return hexString.toString();

    } catch (NoSuchAlgorithmException e) {
        e.printStackTrace();
    }
        return "";
    }
}