package com.kustacks.kuring.worker.updater.api.notice;

public interface KuisAuthManager {
    String getSessionId();
    void forceRenewing();
}