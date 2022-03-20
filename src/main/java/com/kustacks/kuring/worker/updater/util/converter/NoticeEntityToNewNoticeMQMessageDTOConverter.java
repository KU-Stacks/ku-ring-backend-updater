package com.kustacks.kuring.worker.updater.util.converter;

import com.kustacks.kuring.worker.persistence.notice.Notice;
import com.kustacks.kuring.worker.updater.CategoryName;
import com.kustacks.kuring.worker.updater.mq.dto.NewNoticeMQMessageDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class NoticeEntityToNewNoticeMQMessageDTOConverter implements DTOConverter<NewNoticeMQMessageDTO, Notice> {

    @Value("${notice.normal-base-url}")
    private String normalBaseUrl;

    @Value("${notice.library-base-url}")
    private String libraryBaseUrl;

    @Override
    public NewNoticeMQMessageDTO convert(Notice target) {
        return NewNoticeMQMessageDTO.builder()
                .articleId(target.getArticleId())
                .postedDate(target.getPostedDate())
                .subject(target.getSubject())
                .category(target.getCategory().getName())
                .baseUrl(CategoryName.LIBRARY.getName().equals(target.getCategory().getName()) ? libraryBaseUrl : normalBaseUrl)
                .fullUrl("") // 현재 지원하지 않는 기능
                .build();
    }
}
