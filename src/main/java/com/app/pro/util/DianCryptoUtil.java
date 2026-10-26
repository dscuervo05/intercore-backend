package com.app.pro.util;

import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Component
public class DianCryptoUtil {

    public String generarHashSha384(String cadenaAEncriptar){
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-384");
            byte[] hash = digest.digest(cadenaAEncriptar.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder( 2 * hash.length );
            for(byte b : hash){
                String hex = Integer.toHexString(0xff & b);
                if(hex.length()==1){
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        }catch (NoSuchAlgorithmException e){
            throw new RuntimeException( "Error inicializando SHA-384" , e);
        }
    }
}
