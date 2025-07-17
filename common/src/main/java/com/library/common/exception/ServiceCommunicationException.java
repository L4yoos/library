package com.library.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class ServiceCommunicationException extends RuntimeException {
    public ServiceCommunicationException(String serviceName, String message) {
        super("Failed to communicate with " + serviceName + " service: " + message);
    }
}