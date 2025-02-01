package com.vider.quantum.engine.exception;

public class QuantumException extends RuntimeException {

    public QuantumException() {
        super();
    }

    public QuantumException(String message) {
        super(message);
    }

    public QuantumException(Throwable e) {
        super(e);
    }
}
