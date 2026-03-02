package com.hieu.ms.shared.configuration;

public enum VNPayCommand {
    PAY("pay"),
    REFUND("refund"),
    QUERY("querydr");

    private final String value;

    VNPayCommand(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
