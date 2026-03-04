package com.closer.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ContactLimitExceededException extends RuntimeException {

    public ContactLimitExceededException(int limit) {
        super("Contact limit of " + limit + " has been reached. Please remove inactive contacts before adding new ones.");
    }

    public ContactLimitExceededException(String message) {
        super(message);
    }
}
