package com.kustacks.kuring.worker.updater.mq.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdaterRequestMQMessageDTO {

    @JsonProperty("type")
    private String type;
}
