package com.kustacks.kuring.worker.updater.mq;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

public interface MQNotifierProducer<T> {
    void publish(T messageDTO) throws IOException, TimeoutException;
    void publish(List<T> messageDTOList) throws IOException, TimeoutException;
}
