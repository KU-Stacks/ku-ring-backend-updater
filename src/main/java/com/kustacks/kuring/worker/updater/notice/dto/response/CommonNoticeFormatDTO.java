package com.kustacks.kuring.worker.updater.notice.dto.response;

import lombok.*;

import java.util.Objects;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommonNoticeFormatDTO {

    private String articleId;

    private String updatedDate;

    private String subject;

    @Setter
    private String postedDate;

    @Setter
    private String baseUrl;

    @Setter
    private String fullUrl;

    public boolean isEquals(CommonNoticeFormatDTO c) {
        return Objects.equals(this.articleId, c.articleId) &&
                Objects.equals(this.postedDate, c.postedDate) &&
                Objects.equals(this.updatedDate, c.updatedDate) &&
                Objects.equals(this.subject, c.subject);
    }
}
