package com.jupiter.tusa.newmap.event;

public interface MapSignatureEvent<T> {
    void handle(T input);
}
