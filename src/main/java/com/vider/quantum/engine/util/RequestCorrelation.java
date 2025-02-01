package com.vider.quantum.engine.util;

/**
 * Utility class which stores ThreadLocal (Request) correlation Id.
 */
public class RequestCorrelation {

    private static final ThreadLocal<String> id = new ThreadLocal<String>();

    public static void setId(String correlationId) {
        id.set(correlationId);
    }

    public static String getId() {
        return id.get();
    }
}