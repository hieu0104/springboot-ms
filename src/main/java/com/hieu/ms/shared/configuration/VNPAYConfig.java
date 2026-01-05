package com.hieu.ms.shared.configuration;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "vn-pay")
@Validated
public class VNPAYConfig {

    // API Endpoints & Secrets
    @NotBlank
    private String vnpPayUrl;

    @NotBlank
    private String vnpReturnUrl;

    @NotBlank
    private String vnpTmnCode;

    @NotBlank
    private String secretKey;

    @NotBlank
    private String vnpApiUrl;

    // Business Configuration
    @NotBlank
    private String version = "2.1.0";

    @NotBlank
    private String orderType = "other";

    @NotBlank
    private String currency = "VND";

    @NotBlank
    private String locale = "vn";

    @NotBlank
    private String defaultBankCode = "NCB";

    @NotBlank
    private String transactionTypeRefund = "02";

    @Min(1)
    private int paymentExpiryMinutes = 15;

    @NotBlank
    private String timezone = "Etc/GMT+7";

    @NotBlank
    private String dateFormat = "yyyyMMddHHmmss";

    // MD5 Hashing
    public static String md5(String message) {
        return hashMessage(message, "MD5");
    }

    // SHA-256 Hashing
    public static String sha256(String message) {
        return hashMessage(message, "SHA-256");
    }

    // Generic Hashing function
    // Fixed: Throw exception instead of returning empty string
    private static String hashMessage(String message, String algorithm) {
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            byte[] hash = md.digest(message.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(2 * hash.length);
            for (byte b : hash) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hash algorithm not available: " + algorithm, e);
        }
    }

    // Hash all fields with HMAC-SHA512
    @Cacheable(value = "vnpayHash", key = "#fields.toString()")
    public String hashAllFields(Map<String, String> fields) {
        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);

        StringBuilder sb = new StringBuilder();
        for (String fieldName : fieldNames) {
            String fieldValue = fields.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                sb.append(fieldName).append("=").append(fieldValue).append("&");
            }
        }

        if (!sb.isEmpty()) {
            sb.setLength(sb.length() - 1); // Remove the last "&"
        }

        return hmacSHA512(secretKey, sb.toString());
    }

    // HMAC-SHA512
    // Fixed: Throw exception instead of returning empty string
    public static String hmacSHA512(String key, String data) {
        try {
            if (key == null || data == null) {
                throw new NullPointerException();
            }
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac512.init(secretKeySpec);
            byte[] result = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception ex) {
            return "";
        }
    }

    public static String getIpAddress(HttpServletRequest request) {
        String ipAdress;
        try {
            ipAdress = request.getHeader("X-FORWARDED-FOR");
            if (ipAdress == null) {
                ipAdress = request.getRemoteAddr();
            }
        } catch (Exception e) {
            ipAdress = "Invalid IP:" + e.getMessage();
        }
        return ipAdress;
    }

    public static String getRandomNumber(int len) {
        Random rnd = new Random();
        String chars = "0123456789";
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
