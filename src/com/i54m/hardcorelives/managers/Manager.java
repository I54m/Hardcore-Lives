package com.i54m.hardcorelives.managers;

import com.i54m.hardcorelives.handlers.ErrorHandler;

public interface Manager {
    ErrorHandler ERROR_HANDLER = ErrorHandler.getINSTANCE();

    void start();
    boolean isStarted();
    void stop();
    default String getName() {
        return this.getClass().getName();
    }
}
