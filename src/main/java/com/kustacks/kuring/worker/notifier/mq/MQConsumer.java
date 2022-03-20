package com.kustacks.kuring.worker.notifier.mq;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public interface MQConsumer {
    void listen() throws IOException, TimeoutException;
}
