package com.library.notificationservice.service;

public interface EmailContentFormatter<T> {
    String format(T event);
}
