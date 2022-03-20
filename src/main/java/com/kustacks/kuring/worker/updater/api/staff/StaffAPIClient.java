package com.kustacks.kuring.worker.updater.api.staff;

import com.kustacks.kuring.worker.error.InternalLogicException;

import java.util.List;

public interface StaffAPIClient<T, K> {
    int SCRAP_TIMEOUT = 20000;

    List<T> request(K k) throws InternalLogicException;
}
