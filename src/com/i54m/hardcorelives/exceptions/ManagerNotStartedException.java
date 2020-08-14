package com.i54m.hardcorelives.exceptions;

import com.i54m.hardcorelives.managers.Manager;

public class ManagerNotStartedException extends Exception {

    private final Manager manager;
    private Manager requiredManager = null;

    public ManagerNotStartedException(Manager requiredManager, Manager manager) {
        this.requiredManager = requiredManager;
        this.manager = manager;
    }

    public ManagerNotStartedException(Manager manager) {
        this.manager = manager;
    }

    @Override
    public String getMessage() {
        if (requiredManager == null)
            return manager.getName() + " has not been started! It must be started using the provided start() method," +
                    " this will initialize the manager correctly and ensure that only one instance of it is running at a time.";
        else
            return manager.getName() + " requires " + requiredManager.getName() + " to be started before it as " + manager.getName()
                    + " depends on methods from " + requiredManager.getName() + " in order to be used correctly. " + requiredManager.getName()
                    + " must be started using the provided start() method, this will initialize the manager correctly and ensure that only one instance of it is running at a time.";
    }
}
