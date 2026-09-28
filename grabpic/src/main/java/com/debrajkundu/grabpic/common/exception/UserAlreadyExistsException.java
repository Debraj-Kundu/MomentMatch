package com.debrajkundu.grabpic.common.exception;

public class UserAlreadyExistsException extends RuntimeException{
    public UserAlreadyExistsException(String email) {
        super("User already exists with email: " + email);
    }
}
