package com.example.pinokkio.common.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class AESUtil {

    /**
     * 주어진 키를 사용하여 암호화된 데이터를 복호화합니다.
     *
     * @param key           복호화에 사용할 키 (Base64 인코딩된 문자열)
     * @param encryptedData 복호화할 데이터 (Base64 인코딩된 문자열)
     * @return 복호화된 문자열
     * @throws Exception 복호화 과정에서 발생할 수 있는 예외
     */
    public static String decrypt(String key, String encryptedData) throws Exception {
        // Base64로 인코딩된 키를 디코딩
        byte[] decodedKey = Base64.getDecoder().decode(key);
        // Base64로 인코딩된 암호화 데이터를 디코딩
        byte[] decodedData = Base64.getDecoder().decode(encryptedData);

        // AES 암호화에 사용할 비밀키 생성
        SecretKeySpec secretKey = new SecretKeySpec(decodedKey, "AES");
        // AES/CBC/PKCS5Padding 모드의 Cipher 객체 생성
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

        // 초기화 벡터(IV) 추출 (암호화된 데이터의 첫 16바이트)
        byte[] iv = new byte[16];
        System.arraycopy(decodedData, 0, iv, 0, 16);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

        // Cipher 객체 초기화 (복호화 모드)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);
        // 복호화 수행 (IV를 제외한 나머지 데이터)
        byte[] decryptedBytes = cipher.doFinal(decodedData, 16, decodedData.length - 16);
        // 복호화된 바이트 배열을 문자열로 변환하여 반환
        return new String(decryptedBytes);
    }

}
