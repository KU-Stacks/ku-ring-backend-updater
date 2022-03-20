package com.kustacks.kuring.worker.updater.util.converter;

public interface DTOConverter<T, K> {
    T convert(K target);
}
