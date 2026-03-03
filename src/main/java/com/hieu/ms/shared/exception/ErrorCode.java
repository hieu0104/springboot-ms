package com.hieu.ms.shared.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import lombok.Getter;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "Uncategorized error", HttpStatus.BAD_REQUEST),
    USER_EXISTED(1002, "User existed", HttpStatus.BAD_REQUEST),
    USERNAME_INVALID(1003, "Username must be at least {min} characters", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1004, "Password must be at least {min} characters", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1005, "User not existed", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1006, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1007, "You do not have permission", HttpStatus.FORBIDDEN),
    INVALID_DOB(1008, "Your age must be at least {min}", HttpStatus.BAD_REQUEST),
    INVITATION_NOT_FOUND(1009, "Invitation not found", HttpStatus.NOT_FOUND),
    INVITATION_EXPIRED(1010, "Invitation has expired", HttpStatus.BAD_REQUEST),
    INVITATION_ALREADY_ACCEPTED(1011, "Invitation already accepted", HttpStatus.BAD_REQUEST),
    INVITATION_ALREADY_EXISTS(1012, "Invitation already exists for this email and project", HttpStatus.BAD_REQUEST),
    USER_ALREADY_IN_PROJECT(1013, "User is already a member of this project", HttpStatus.BAD_REQUEST),
    PROJECT_NOT_FOUND(1014, "Project not found", HttpStatus.NOT_FOUND),
    EMAIL_SEND_FAILED(1015, "Failed to send invitation email", HttpStatus.INTERNAL_SERVER_ERROR),
    INVITATION_ALREADY_SENT(1016, "Invitation already sent to this email for this project", HttpStatus.BAD_REQUEST),
    USER_ALREADY_MEMBER(1017, "User is already a member of this project", HttpStatus.BAD_REQUEST),
    INVITATION_EMAIL_MISMATCH(1018, "This invitation is for a different email address", HttpStatus.FORBIDDEN),
    INVITATION_ALREADY_PROCESSED(1019, "This invitation has already been processed", HttpStatus.BAD_REQUEST),
    INVALID_REQUEST(1020, "Invalid request data", HttpStatus.BAD_REQUEST),
    PAYMENT_PROVIDER_NOT_FOUND(2001, "Payment provider not found", HttpStatus.BAD_REQUEST),
    INVALID_PAYMENT_AMOUNT(2002, "Invalid payment amount", HttpStatus.BAD_REQUEST),
    PAYMENT_NOT_FOUND(2003, "Payment not found", HttpStatus.NOT_FOUND),
    PAYMENT_ALREADY_PROCESSED(2004, "Payment has already been processed", HttpStatus.BAD_REQUEST),
    INVALID_PAYMENT_SIGNATURE(2005, "Invalid payment signature", HttpStatus.BAD_REQUEST),
    SUBSCRIPTION_NOT_FOUND(3001, "Subscription not found", HttpStatus.NOT_FOUND),
    INVALID_SUBSCRIPTION_PLAN(3002, "Invalid subscription plan", HttpStatus.BAD_REQUEST),
    ISSUE_NOT_FOUND(4001, "Issue not found", HttpStatus.NOT_FOUND),
    INVALID_TRANSITION(4002, "Invalid status transition", HttpStatus.BAD_REQUEST);

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;
}
