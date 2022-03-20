package com.kustacks.kuring.worker.updater.mq;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kustacks.kuring.worker.updater.CategoryName;
import com.kustacks.kuring.worker.updater.mq.dto.NoticeUpdateRequestMQMessageDTO;
import com.kustacks.kuring.worker.updater.mq.dto.StaffUpdaterRequestMQMessageDTO;
import com.kustacks.kuring.worker.updater.mq.dto.UserUpdaterRequestMQMessageDTO;
import com.kustacks.kuring.worker.updater.notice.AllNoticeUpdater;
import com.kustacks.kuring.worker.updater.notice.NewNoticeUpdater;
import com.kustacks.kuring.worker.updater.staff.StaffUpdater;
import com.kustacks.kuring.worker.updater.user.UserUpdater;
import com.rabbitmq.client.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@Slf4j
@Component
public class RabbitMQUpdaterConsumer implements MQConsumer {

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

    private final NewNoticeUpdater newNoticeUpdater;
    private final AllNoticeUpdater allNoticeUpdater;
    private final StaffUpdater staffUpdater;
    private final UserUpdater userUpdater;

    private final ObjectMapper objectMapper;
    private final Map<String, CategoryName> stringCategoryNameMap;
    private final Map<CategoryName, ThreadPoolTaskExecutor> categoryNameThreadPoolTaskExecutorMap;
    private final ThreadPoolTaskExecutor staffUpdaterThreadTaskExecutor;
    private final ThreadPoolTaskExecutor userUpdaterThreadTaskExecutor;

    private static final String EXCHANGE_NAME = "amq.direct";
    private static final String ROUTING_KEY_NOTICE = "updater.notice";
    private static final String ROUTING_KEY_STAFF = "updater.staff";
    private static final String ROUTING_KEY_USER = "updater.user";

    private static final String QUEUE_NAME_NOTICE = "updater.notice";
    private static final String QUEUE_NAME_STAFF = "updater.staff";
    private static final String QUEUE_NAME_USER = "updater.user";
    private static final String TYPE_NOTICE_NEW = "new";
    private static final String TYPE_NOTICE_MODIFY_REMOVE = "modify-remove";

    public RabbitMQUpdaterConsumer(NewNoticeUpdater newNoticeUpdater,
                                   AllNoticeUpdater allNoticeUpdater,
                                   StaffUpdater staffUpdater,
                                   UserUpdater userUpdater,
                                   Map<String, CategoryName> stringCategoryNameMap,
                                   Map<CategoryName, ThreadPoolTaskExecutor> categoryNameThreadPoolTaskExecutorMap,
                                   ThreadPoolTaskExecutor staffUpdaterThreadTaskExecutor,
                                   ThreadPoolTaskExecutor userUpdaterThreadTaskExecutor,
                                   ObjectMapper objectMapper
    ) {
        this.newNoticeUpdater = newNoticeUpdater;
        this.allNoticeUpdater = allNoticeUpdater;
        this.staffUpdater = staffUpdater;
        this.userUpdater = userUpdater;
        this.stringCategoryNameMap = stringCategoryNameMap;
        this.categoryNameThreadPoolTaskExecutorMap = categoryNameThreadPoolTaskExecutorMap;
        this.staffUpdaterThreadTaskExecutor = staffUpdaterThreadTaskExecutor;
        this.userUpdaterThreadTaskExecutor = userUpdaterThreadTaskExecutor;
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

        initChannel(connection, EXCHANGE_NAME, ROUTING_KEY_NOTICE, QUEUE_NAME_NOTICE, "notice-updater",
                (consumerTag, envelope, properties, body) -> {

                    log.info("{} 수신한 메시지 = {}", QUEUE_NAME_NOTICE, new String(body, StandardCharsets.UTF_8));

                    try {
                        NoticeUpdateRequestMQMessageDTO message = objectMapper.readValue(body, NoticeUpdateRequestMQMessageDTO.class);
                        String type = message.getType();
                        String category = message.getCategory();
                        CategoryName categoryName = stringCategoryNameMap.get(category);
                        if(TYPE_NOTICE_NEW.equals(type)) {
                            categoryNameThreadPoolTaskExecutorMap.get(categoryName).execute(() -> newNoticeUpdater.update(categoryName, true));
                        } else if(TYPE_NOTICE_MODIFY_REMOVE.equals(type)) {
                            categoryNameThreadPoolTaskExecutorMap.get(categoryName).execute(() -> allNoticeUpdater.update(categoryName, false));
                        } else {
                            log.error("알 수 없는 notice message type = {}", type);
                        }
                    } catch(JsonParseException e) {
                        log.error("MQ 메세지를 파싱 중 문제가 발생했습니다.", e);
                    }
        });

        initChannel(connection, EXCHANGE_NAME, ROUTING_KEY_STAFF, QUEUE_NAME_STAFF, "staff-updater",
                (consumerTag, envelope, properties, body) -> {

                    log.info("{}로부터 수신한 메시지 = {}", QUEUE_NAME_STAFF, new String(body, StandardCharsets.UTF_8));

                    try {
                        StaffUpdaterRequestMQMessageDTO message = objectMapper.readValue(body, StaffUpdaterRequestMQMessageDTO.class);
                        String type = message.getType();

                        if("all".equals(type)) {
                            String categoryNameStr = message.getCategory();
                            CategoryName categoryNameEnum = stringCategoryNameMap.get(categoryNameStr);
                            staffUpdaterThreadTaskExecutor.execute(() -> staffUpdater.update(categoryNameEnum));
                        } else {
                            log.error("알 수 없는 staff message type = {}", type);
                        }
                    } catch(JsonParseException e) {
                        log.error("MQ 메세지를 파싱 중 문제가 발생했습니다.", e);
                    }
        });

        initChannel(connection, EXCHANGE_NAME, ROUTING_KEY_USER, QUEUE_NAME_USER, "user-updater",
                (consumerTag, envelope, properties, body) -> {

                    log.info("{}로부터 수신한 메시지 = {}", QUEUE_NAME_USER, new String(body, StandardCharsets.UTF_8));

                    try {
                        UserUpdaterRequestMQMessageDTO message = objectMapper.readValue(body, UserUpdaterRequestMQMessageDTO.class);
                        String type = message.getType();
                        if("validation".equals(type)) {
                            userUpdaterThreadTaskExecutor.execute(userUpdater::update);
                        } else {
                            log.error("알 수 없는 user message type = {}", type);
                        }
                    } catch(JsonParseException e) {
                        log.error("MQ 메세지를 파싱 중 문제가 발생했습니다.", e);
                    }
        });
    }

    private void initChannel(Connection connection, String exchange, String routingKey, String queueName, String consumerTag, MQMessageHandler handler) throws IOException {
        Channel channel = connection.createChannel();
        channel.exchangeDeclare(exchange, "direct", true);
        channel.queueDeclare(queueName, false, false, false, null);
        channel.queueBind(queueName, exchange, routingKey);
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
