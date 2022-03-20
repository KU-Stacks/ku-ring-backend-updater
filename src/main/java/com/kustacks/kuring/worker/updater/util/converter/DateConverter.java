package com.kustacks.kuring.worker.updater.util.converter;

public interface DateConverter<T, K> {
    T convert(K k);
}
