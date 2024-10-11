package com.je.debugerojsandbox.security;

import java.security.Permission;

public class DefaultSecurityManager extends SecurityManager {
    @Override
    public void checkPermission(Permission perm) {
        // allow anything.
    }

}
