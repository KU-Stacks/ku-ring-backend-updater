package com.kustacks.kuring.worker.notifier.mq;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kustacks.kuring.worker.notifier.FCMMessageSender;
import com.kustacks.kuring.worker.notifier.mq.dto.AdminMQMessageDTO;
import com.kustacks.kuring.worker.notifier.mq.dto.NewNoticeMQMessageDTO;
import com.rabbitmq.client.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

@Slf4j
@Component
public class RabbitMQNotifierConsumer implements MQConsumer {

    @Value("${spring.rabbitmq.host}")
    private String host;

    @Value("${spring.rabbitmq.port}")
    private int port;

    @Value("${spring.rabbitmq.username}")
    private String username;

    @Value("${spring.rabbitmq.virtual-host}")
    private String vhost;

    @Value("${spring.rabbitmq.password}")
    private String password;

    private final FCMMessageSender fcmMessageSender;
    private final ObjectMapper objectMapper;

    private static final String EXCHANGE_NAME = "amq.direct";
    private static final String ROUTING_KEY_NOTICE = "notification.notice";
    private static final String ROUTING_KEY_ADMIN = "notification.admin";
    private static final String QUEUE_NAME_NOTICE = "notification.notice";
    private static final String QUEUE_NAME_ADMIN = "notification.admin";

    public RabbitMQNotifierConsumer(ObjectMapper objectMapper, FCMMessageSender fcmMessageSender) {
        this.fcmMessageSender = fcmMessageSender;
        this.objectMapper = objectMapper;
    }

    public void listen() throws IOException, TimeoutException {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setUsername(username);
        factory.setPassword(password);
        factory.setVirtualHost(vhost);
        factory.setHost(host);
        factory.setPort(port);
        factory.setConnectionTimeout(3000);

        Connection connection = factory.newConnection();

        initChannel(connection, EXCHANGE_NAME, ROUTING_KEY_NOTICE, QUEUE_NAME_NOTICE, "notifier-notice",
                (consumerTag, envelope, properties, body) -> {

                    log.info("{} 수신한 메시지 = {}", QUEUE_NAME_NOTICE, new String(body, StandardCharsets.UTF_8));

                    try {
                        NewNoticeMQMessageDTO message = objectMapper.readValue(body, NewNoticeMQMessageDTO.class);
                        fcmMessageSender.send(message);
                    } catch(JsonParseException e) {
                        log.error("MQ 메세지를 파싱 중 문제가 발생했습니다.", e);
                    }
        });

        initChannel(connection, EXCHANGE_NAME, ROUTING_KEY_ADMIN, QUEUE_NAME_ADMIN, "notifier-admin",
                (consumerTag, envelope, properties, body) -> {

                    log.info("{} 수신한 메시지 = {}", QUEUE_NAME_NOTICE, new String(body, StandardCharsets.UTF_8));

                    try {
                        AdminMQMessageDTO message = objectMapper.readValue(body, AdminMQMessageDTO.class);
                        fcmMessageSender.send(message);
                    } catch(JsonParseException e) {
                        log.error("MQ 메세지를 파싱 중 문제가 발생했습니다.", e);
                    }
                });
    }

    private void initChannel(Connection connection, String exchange, String routingkey, String queueName, String consumerTag, MQMessageHandler handler) throws IOException {
        Channel channel = connection.createChannel();
        channel.exchangeDeclare(exchange, "direct", true);
        channel.queueDeclare(queueName, true, false, false, null);
        channel.queueBind(queueName, exchange, routingkey);
        channel.basicConsume(queueName, true, consumerTag, new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag,
                                       Envelope envelope,
                                       AMQP.BasicProperties properties,
                                       byte[] body) throws IOException {
                handler.handle(consumerTag, envelope, properties, body);
            }
        });
    }

    interface MQMessageHandler {
        void handle(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException;
    }
}
