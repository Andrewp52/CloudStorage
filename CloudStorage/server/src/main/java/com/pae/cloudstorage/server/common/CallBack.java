package com.pae.cloudstorage.server.common;

@FunctionalInterface
public interface CallBack {
    void call(Object... args);
}
