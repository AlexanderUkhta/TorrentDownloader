package com.visearch.config;

import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

public class ConfigConstantsAndMethods {

    public static final Integer MAGNET_LINK_INITIALIAZED = 1;
    public static final Integer MAGNET_LINK_IN_PROCESS = 2;
    public static final Integer MAGNET_LINK_PROCESS_SUCCEEDED = 0;
    public static final Integer MAGNET_LINK_PROCESS_FAILED = -1;

    public static void ultimateWhile(Supplier<Boolean> condition, Integer timeoutSeconds) throws TimeoutException {
        Long start = System.currentTimeMillis();
        Long end = 0L;

        while (condition.get() && ((end - start) / 1000) < timeoutSeconds) {
            end = System.currentTimeMillis();
        }

        if  (((end - start) / 1000) >= timeoutSeconds) {
            throw new TimeoutException();
        }
    }
}
