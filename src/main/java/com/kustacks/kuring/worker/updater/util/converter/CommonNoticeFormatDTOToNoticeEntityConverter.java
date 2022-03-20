package com.kustacks.kuring.worker.updater.util.converter;

import com.kustacks.kuring.worker.persistence.notice.Notice;
import com.kustacks.kuring.worker.updater.notice.dto.response.CommonNoticeFormatDTO;
import org.springframework.stereotype.Component;

@Component
public class CommonNoticeFormatDTOToNoticeEntityConverter implements DTOConverter<Notice, CommonNoticeFormatDTO> {

    @Override
    public Notice convert(CommonNoticeFormatDTO target) {
        return Notice.builder()
                .articleId(target.getArticleId())
                .postedDate(target.getPostedDate())
                .updatedDate(target.getUpdatedDate())
                .subject(target.getSubject())
                .baseUrl(target.getBaseUrl())
                .fullUrl(target.getFullUrl())
                .build();
    }
}
