package com.kustacks.kuring.worker.updater.mq;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public interface MQConsumer {
    void listen() throws IOException, TimeoutException;
}
