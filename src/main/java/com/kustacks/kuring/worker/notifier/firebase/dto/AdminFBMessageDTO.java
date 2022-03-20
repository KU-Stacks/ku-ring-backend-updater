package com.kustacks.kuring.worker.notifier.firebase.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Builder
public class AdminFBMessageDTO extends FBMessageDTO {

    @JsonProperty("title")
    private String title;

    @JsonProperty("body")
    private String body;

    public AdminFBMessageDTO(String title, String body) {
        this.type = "admin";
        this.title = title;
        this.body = body;
    }
}
