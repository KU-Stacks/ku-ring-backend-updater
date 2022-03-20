package com.kustacks.kuring.worker.notifier.mq.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Builder
public class AdminMQMessageDTO extends MQMessageDTO {

    @JsonProperty("title")
    private String title;

    @JsonProperty("body")
    private String body;

    @Builder
    public AdminMQMessageDTO(String title, String body) {
        this.type = "admin";
        this.title = title;
        this.body = body;
    }
}
