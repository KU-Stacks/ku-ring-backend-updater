package com.kustacks.kuring.worker.notifier.util.converter;

public interface DTOConverter<T, K> {
    T convert(K target);
}
