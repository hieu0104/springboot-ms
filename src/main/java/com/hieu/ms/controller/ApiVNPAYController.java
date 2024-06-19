package com.hieu.ms.controller;

import com.hieu.ms.dto.request.VNPAYRequest;
import com.hieu.ms.dto.response.VNPayResponse;
import com.hieu.ms.service.VNPayService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 *
 * @author Administrator
 */
@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class ApiVNPAYController {

    private final VNPayService vnPayService;



    @PostMapping("/public/pay")
    @CrossOrigin
    public String getPay(@RequestBody VNPAYRequest request) throws UnsupportedEncodingException {
        return vnPayService.getPay(request);
    }

    @PostMapping("/public/pay-return")
    @CrossOrigin
    public String getPayReturn(@RequestBody VNPayResponse response) throws UnsupportedEncodingException {
        return vnPayService.getPayReturn(response);
    }

    @GetMapping("/public/processReturnVNPAY")
    @CrossOrigin
    public ResponseEntity<?> processReturnVNPAY(HttpServletRequest request) throws UnsupportedEncodingException {
        return vnPayService.processReturnVNPAY(request);
    }

    @PostMapping("/public/refund")
    @CrossOrigin
    public String refund() throws IOException {
        return vnPayService.refund();
    }

    @PostMapping("/public/querydr")
    @CrossOrigin
    public String queryVNP(@RequestBody Map<String, String> params) throws IOException {
        return vnPayService.queryVNP(params);
    }

}