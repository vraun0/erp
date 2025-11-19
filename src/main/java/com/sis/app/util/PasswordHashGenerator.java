package com.sis.app.util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordHashGenerator {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: PasswordHashGenerator <password>");
            return;
        }
        String password = args[0];
        String hash = BCrypt.hashpw(password, BCrypt.gensalt(10));
        System.out.println(hash);
    }
}
