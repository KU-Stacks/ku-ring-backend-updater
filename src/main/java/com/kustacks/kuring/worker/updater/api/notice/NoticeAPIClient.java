package com.kustacks.kuring.worker.updater.api.notice;

import com.kustacks.kuring.worker.error.InternalLogicException;

import java.util.List;

public interface NoticeAPIClient<T, K> {
    int SCRAP_TIMEOUT = 1200000;

    List<T> request(K k, boolean isFindingNew) throws InternalLogicException;
}