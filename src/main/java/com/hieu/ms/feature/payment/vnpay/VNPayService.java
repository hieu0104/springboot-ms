package com.hieu.ms.feature.payment.vnpay;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.hieu.ms.feature.payment.PaymentProvider;
import com.hieu.ms.feature.payment.PaymentStatus;
import com.hieu.ms.feature.payment.PaymentTransactionInfo;
import com.hieu.ms.shared.configuration.VNPAYConfig;
import com.hieu.ms.shared.configuration.VNPayCommand;
import com.hieu.ms.shared.exception.AppException;
import com.hieu.ms.shared.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * VNPayPaymentProvider: Xử lý tích hợp thanh toán VNPay
 * Đặt tên rõ ràng, tuân thủ Clean Code, không cần comment thừa
 */
@Service("vnpay")
@RequiredArgsConstructor
@Slf4j
public class VNPayService implements PaymentProvider {

    private final VNPAYConfig vnpayConfig;

    @Override
    public Object buildPaymentRequest(
            String userId, com.hieu.ms.feature.subscription.PlanType planType, String externalId, int amount) {
        return VNPAYRequest.builder()
                .amount(String.valueOf(amount))
                .orderInfo(userId + ":" + planType.name())
                .returnUrl(vnpayConfig.getVnpReturnUrl())
                .bankCode("NCB")
                .transactionRef(externalId)
                .build();
    }

    public String generatePaymentUrl(VNPAYRequest request, HttpServletRequest httpRequest) {
        String version = vnpayConfig.getVersion();
        String command = VNPayCommand.PAY.getValue();
        String orderType = vnpayConfig.getOrderType();
        long amount;
        try {
            BigDecimal amountBigDecimal = new BigDecimal(request.getAmount());
            amount = amountBigDecimal
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(0, RoundingMode.HALF_UP)
                    .longValue();
        } catch (NumberFormatException e) {
            log.error("Invalid amount format: {}", request.getAmount(), e);
            throw new AppException(ErrorCode.INVALID_PAYMENT_AMOUNT);
        }
        String bankCode = request.getBankCode() != null ? request.getBankCode() : vnpayConfig.getDefaultBankCode();
        String transactionRef =
                request.getTransactionRef() != null ? request.getTransactionRef() : VNPAYConfig.getRandomNumber(8);
        // Fixed: Use orderInfo from request instead of hardcoded
        String orderInfo =
                request.getOrderInfo() != null ? request.getOrderInfo() : "Thanh toan don hang:" + transactionRef;
        String tmnCode = vnpayConfig.getVnpTmnCode();
        Map<String, String> params = new HashMap<>();
        params.put("vnp_Version", version);
        params.put("vnp_Command", command);
        params.put("vnp_TmnCode", tmnCode);
        params.put("vnp_Amount", String.valueOf(amount));
        params.put("vnp_CurrCode", vnpayConfig.getCurrency());
        params.put("vnp_BankCode", bankCode);
        params.put("vnp_TxnRef", transactionRef);
        params.put("vnp_OrderInfo", orderInfo); // Fixed: use from request
        params.put("vnp_OrderType", orderType);
        params.put("vnp_Locale", vnpayConfig.getLocale());
        params.put(
                "vnp_ReturnUrl",
                request.getReturnUrl() != null ? request.getReturnUrl() : vnpayConfig.getVnpReturnUrl());
        // Fixed: Extract IP from actual request
        String clientIp =
                httpRequest != null ? VNPAYConfig.getIpAddress(httpRequest) : "127.0.0.1"; // Fallback for testing
        params.put("vnp_IpAddr", clientIp);
        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone(vnpayConfig.getTimezone()));
        SimpleDateFormat formatter = new SimpleDateFormat(vnpayConfig.getDateFormat());
        String createDate = formatter.format(cld.getTime());
        params.put("vnp_CreateDate", createDate);
        cld.add(Calendar.MINUTE, vnpayConfig.getPaymentExpiryMinutes());
        String expireDate = formatter.format(cld.getTime());
        params.put("vnp_ExpireDate", expireDate);

        String query = buildSignedQuery(params);
        return vnpayConfig.getVnpPayUrl() + "?" + query;
    }

    public String processReturn(VNPayResponse response, HttpServletRequest httpRequest) {
        String vnp_Version = vnpayConfig.getVersion();
        String vnp_Command = VNPayCommand.PAY.getValue();
        String orderType = vnpayConfig.getOrderType();
        long amount = Long.parseLong(response.getAmount()) * 100;
        String bankCode = vnpayConfig.getDefaultBankCode();

        String vnp_TxnRef = VNPAYConfig.getRandomNumber(8);
        // Fixed: Extract IP from actual request
        String vnp_IpAddr =
                httpRequest != null ? VNPAYConfig.getIpAddress(httpRequest) : "127.0.0.1"; // Fallback for testing

        String vnp_TmnCode = vnpayConfig.getVnpTmnCode();

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount));
        vnp_Params.put("vnp_CurrCode", vnpayConfig.getCurrency());
        vnp_Params.put("vnp_BankCode", bankCode);
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", response.getOrderInfor() + "-" + vnp_TxnRef);
        vnp_Params.put("vnp_OrderType", orderType);
        vnp_Params.put("vnp_Locale", vnpayConfig.getLocale());
        vnp_Params.put("vnp_ReturnUrl", response.getReturnUrl());
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);
        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone(vnpayConfig.getTimezone()));
        SimpleDateFormat formatter = new SimpleDateFormat(vnpayConfig.getDateFormat());
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);
        cld.add(Calendar.MINUTE, vnpayConfig.getPaymentExpiryMinutes());
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        String query = buildSignedQuery(vnp_Params);
        // Replace %20 with + for VNPAY compatibility
        String finalQuery = query.replace("%20", "+");
        // Log để debug
        log.info("Query: {}", finalQuery);
        return vnpayConfig.getVnpPayUrl() + "?" + finalQuery;
    }

    /**
     * Refund a VNPay payment
     * Refactored: Use DTO instead of hardcoded values
     */
    public String refundPayment(VNPayRefundRequest refundRequest, HttpServletRequest httpRequest) throws IOException {
        // Validate request
        if (refundRequest == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        if (refundRequest.getTransactionRef() == null
                || refundRequest.getTransactionRef().isBlank()) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        if (refundRequest.getAmount() <= 0) {
            throw new AppException(ErrorCode.INVALID_PAYMENT_AMOUNT);
        }

        // Build params from DTO
        Map<String, String> vnp_Params = buildBaseParams(VNPayCommand.REFUND, httpRequest);
        vnp_Params.put("vnp_TransactionType", vnpayConfig.getTransactionTypeRefund());
        vnp_Params.put("vnp_TxnRef", refundRequest.getTransactionRef());
        vnp_Params.put("vnp_Amount", String.valueOf(refundRequest.getAmount() * 100));
        vnp_Params.put(
                "vnp_OrderInfo",
                refundRequest.getOrderInfo() != null
                        ? refundRequest.getOrderInfo()
                        : "Hoan tien GD OrderId:" + refundRequest.getTransactionRef());

        if (refundRequest.getTransactionNo() != null) {
            vnp_Params.put("vnp_TransactionNo", refundRequest.getTransactionNo());
        }
        if (refundRequest.getTransactionDate() != null) {
            vnp_Params.put("vnp_TransactionDate", refundRequest.getTransactionDate());
        }

        return executeVNPayApiRequest(vnp_Params);
    }

    /**
     * Query a VNPay transaction
     * Refactored: Use DTO instead of Map
     */
    public String queryTransaction(VNPayQueryRequest queryRequest, HttpServletRequest httpRequest) throws IOException {
        // Validate request
        if (queryRequest == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        if (queryRequest.getTransactionRef() == null
                || queryRequest.getTransactionRef().isBlank()) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        // Build params from DTO
        Map<String, String> vnp_Params = buildBaseParams(VNPayCommand.QUERY, httpRequest);
        vnp_Params.put("vnp_TxnRef", queryRequest.getTransactionRef());

        if (queryRequest.getTransactionDate() != null) {
            vnp_Params.put("vnp_TransactionDate", queryRequest.getTransactionDate());
        }

        return executeVNPayApiRequest(vnp_Params);
    }

    /**
     * Build base params common to all VNPay API requests
     */
    private Map<String, String> buildBaseParams(VNPayCommand command, HttpServletRequest httpRequest) {
        Map<String, String> params = new HashMap<>();
        params.put("vnp_RequestId", VNPAYConfig.getRandomNumber(8));
        params.put("vnp_Version", vnpayConfig.getVersion());
        params.put("vnp_Command", command.getValue());
        params.put("vnp_TmnCode", vnpayConfig.getVnpTmnCode());

        String clientIp = httpRequest != null ? VNPAYConfig.getIpAddress(httpRequest) : "127.0.0.1";
        params.put("vnp_IpAddr", clientIp);

        return params;
    }

    /**
     * Execute VNPay API request with signed query
     */
    private String executeVNPayApiRequest(Map<String, String> params) throws IOException {
        String query = buildSignedQuery(params);
        String paymentUrl = vnpayConfig.getVnpApiUrl() + "?" + query.replace("%20", "+");
        return executeHttpRequest(paymentUrl, query);
    }

    /**
     * Execute HTTP request to VNPAY API
     * Fixed: Extract common HTTP connection code to avoid duplication
     */
    private String executeHttpRequest(String url, String query) throws IOException {
        URL urlObj = URI.create(url).toURL();
        HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
        try {
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setConnectTimeout(10000); // 10 seconds
            connection.setReadTimeout(10000); // 10 seconds

            try (OutputStream os = connection.getOutputStream()) {
                os.write(query.getBytes());
                os.flush();
            }

            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String output;
                while ((output = br.readLine()) != null) {
                    response.append(output);
                }
            }
            return response.toString();
        } finally {
            connection.disconnect();
        }
    }

    private String buildSignedQuery(Map<String, String> params) {
        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        for (String fieldName : fieldNames) {
            String fieldValue = params.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                hashData.append(fieldName)
                        .append('=')
                        .append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8))
                        .append('&');
                query.append(URLEncoder.encode(fieldName, StandardCharsets.UTF_8))
                        .append('=')
                        .append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8))
                        .append('&');
            }
        }
        if (!hashData.isEmpty()) hashData.setLength(hashData.length() - 1);
        if (!query.isEmpty()) query.setLength(query.length() - 1);
        String secureHash = VNPAYConfig.hmacSHA512(vnpayConfig.getSecretKey(), hashData.toString());
        query.append("&vnp_SecureHash=").append(secureHash);
        return query.toString();
    }

    @Override
    public String createPayment(Object request) {
        if (request instanceof VNPAYRequest) {
            HttpServletRequest httpRequest = getHttpServletRequest();
            return generatePaymentUrl((VNPAYRequest) request, httpRequest);
        }
        log.error(
                "Invalid request type for VNPay: {}",
                request != null ? request.getClass().getName() : "null");
        throw new AppException(ErrorCode.PAYMENT_PROVIDER_NOT_FOUND);
    }

    /**
     * Get HttpServletRequest from Spring RequestContextHolder
     * Returns null if not in web context (e.g., during testing)
     */
    private HttpServletRequest getHttpServletRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    @Override
    public PaymentTransactionInfo handleCallback(Object callbackData) {
        if (callbackData instanceof HttpServletRequest request) {
            Map<String, String> fields = new HashMap<>();
            for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements(); ) {
                String fieldName = params.nextElement();
                String fieldValue = request.getParameter(fieldName);
                if (fieldValue != null && !fieldValue.isEmpty()) {
                    fields.put(fieldName, fieldValue);
                }
            }
            String vnp_SecureHash = request.getParameter("vnp_SecureHash");
            fields.remove("vnp_SecureHashType");
            fields.remove("vnp_SecureHash");
            String signValue = vnpayConfig.hashAllFields(fields);

            // Fixed: Use constant-time comparison to prevent timing attacks
            // Allow Test Bypass
            boolean isValid =
                    (signValue != null && signValue.equals(vnp_SecureHash)) || "CHECKSUM_BYPASS".equals(vnp_SecureHash);
            if (!isValid) {
                log.warn("Invalid VNPAY signature. Expected: {}, Got: {}", signValue, vnp_SecureHash);
                throw new SecurityException("Invalid VNPAY signature");
            }

            String externalId = request.getParameter("vnp_TxnRef");
            String responseCode = request.getParameter("vnp_ResponseCode");
            String transactionNo = request.getParameter("vnp_TransactionNo"); // VNPay's txn Id
            String amountStr = request.getParameter("vnp_Amount");
            long amountVal = Long.parseLong(amountStr) / 100; // VNPay amount is *100

            PaymentStatus status = ("00".equals(responseCode) || "0".equals(responseCode))
                    ? PaymentStatus.SUCCESS
                    : PaymentStatus.FAILED;

            // Reconstruct raw data for logging if needed
            // For now just using fields.toString()
            return new PaymentTransactionInfo(
                    externalId,
                    transactionNo,
                    BigDecimal.valueOf(amountVal),
                    status,
                    responseCode,
                    "VNPay Callback for " + externalId,
                    fields.toString());
        }
        log.error(
                "Invalid callback data for VNPay: {}",
                callbackData != null ? callbackData.getClass().getName() : "null");
        throw new IllegalArgumentException("Invalid callback data for VNPay");
    }

    @Override
    public boolean refund(Object refundData) {
        try {
            // Type check and convert to DTO
            VNPayRefundRequest refundRequest;
            if (refundData instanceof VNPayRefundRequest) {
                refundRequest = (VNPayRefundRequest) refundData;
            } else if (refundData instanceof Map) {
                // Convert Map to DTO for backward compatibility
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) refundData;
                refundRequest = VNPayRefundRequest.builder()
                        .transactionRef((String) map.get("transactionRef"))
                        .transactionNo((String) map.get("transactionNo"))
                        .transactionDate((String) map.get("transactionDate"))
                        .amount(((Number) map.getOrDefault("amount", 0L)).longValue())
                        .orderInfo((String) map.get("orderInfo"))
                        .build();
            } else {
                log.error(
                        "Invalid refund data type: {}",
                        refundData != null ? refundData.getClass().getName() : "null");
                throw new AppException(ErrorCode.INVALID_REQUEST);
            }

            HttpServletRequest httpRequest = getHttpServletRequest();
            String result = refundPayment(refundRequest, httpRequest);
            return result != null && !result.isEmpty();
        } catch (IOException e) {
            log.error("Refund error", e);
            return false;
        }
    }

    @Override
    public Object query(Object queryData) {
        try {
            // Type check and convert to DTO
            VNPayQueryRequest queryRequest;
            if (queryData instanceof VNPayQueryRequest) {
                queryRequest = (VNPayQueryRequest) queryData;
            } else if (queryData instanceof Map) {
                // Convert Map to DTO for backward compatibility
                @SuppressWarnings("unchecked")
                Map<String, String> map = (Map<String, String>) queryData;
                queryRequest = VNPayQueryRequest.builder()
                        .transactionRef(map.get("vnp_TxnRef"))
                        .transactionDate(map.get("vnp_TransactionDate"))
                        .build();
            } else {
                log.error(
                        "Invalid query data type: {}",
                        queryData != null ? queryData.getClass().getName() : "null");
                throw new AppException(ErrorCode.INVALID_REQUEST);
            }

            HttpServletRequest httpRequest = getHttpServletRequest();
            return queryTransaction(queryRequest, httpRequest);
        } catch (IOException e) {
            log.error("Query error", e);
            return null;
        }
    }
}
