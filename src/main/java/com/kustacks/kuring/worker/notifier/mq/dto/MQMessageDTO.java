package com.kustacks.kuring.worker.notifier.mq.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class MQMessageDTO {

    @JsonProperty("type")
    protected String type;

    @JsonProperty("token")
    protected String token;
}
