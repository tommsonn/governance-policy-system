package com.governance.exception;

public class InvalidPolicyStatusTransitionException extends RuntimeException {
    public InvalidPolicyStatusTransitionException(String message) {
        super(message);
    }
}