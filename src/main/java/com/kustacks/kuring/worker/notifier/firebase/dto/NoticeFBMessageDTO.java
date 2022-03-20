package com.kustacks.kuring.worker.notifier.firebase.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@Builder
public class NoticeFBMessageDTO extends FBMessageDTO {

    @JsonProperty("articleId")
    private String articleId;

    @JsonProperty("postedDate")
    private String postedDate;

    @JsonProperty("subject")
    private String subject;

    @JsonProperty("category")
    private String category;

    @JsonProperty("baseUrl")
    private String baseUrl;

    @JsonProperty("fullUrl")
    private String fullUrl;

    public NoticeFBMessageDTO(String articleId, String postedDate, String subject, String category, String baseUrl, String fullUrl) {
        this.type = "notice";
        this.articleId = articleId;
        this.postedDate = postedDate;
        this.subject = subject;
        this.category = category;
        this.baseUrl = baseUrl;
        this.fullUrl = fullUrl;
    }
}
