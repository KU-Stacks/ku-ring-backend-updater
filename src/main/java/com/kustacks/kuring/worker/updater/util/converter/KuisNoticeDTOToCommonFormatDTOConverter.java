package com.kustacks.kuring.worker.updater.util.converter;

import com.kustacks.kuring.worker.updater.notice.dto.response.CommonNoticeFormatDTO;
import com.kustacks.kuring.worker.updater.notice.dto.response.KuisNoticeDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class KuisNoticeDTOToCommonFormatDTOConverter implements DTOConverter<CommonNoticeFormatDTO, KuisNoticeDTO> {

    @Value("${notice.normal-base-url}")
    private String baseUrl;

    @Override
    public CommonNoticeFormatDTO convert(KuisNoticeDTO target) {
        return CommonNoticeFormatDTO.builder()
                .articleId(target.getArticleId())
                .postedDate(target.getPostedDate())
                .updatedDate(null)
                .subject(target.getSubject())
                .baseUrl(baseUrl)
                .fullUrl(baseUrl+"?id="+target.getArticleId())
                .build();
    }
}
