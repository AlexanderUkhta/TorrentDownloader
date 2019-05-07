package com.visearch.config;

import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

public class ConfigConstantsAndMethods {

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
