package com.kustacks.kuring.worker.updater.util.converter;

import com.kustacks.kuring.worker.updater.CategoryName;
import com.kustacks.kuring.worker.notifier.firebase.dto.NoticeFBMessageDTO;
import com.kustacks.kuring.worker.persistence.notice.Notice;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class NoticeEntityToNoticeMessageDTOConverter implements DTOConverter<NoticeFBMessageDTO, Notice> {

    @Value("${notice.normal-base-url}")
    private String normalBaseUrl;

    @Value("${notice.library-base-url}")
    private String libraryBaseUrl;

    @Override
    public NoticeFBMessageDTO convert(Notice target) {
        return NoticeFBMessageDTO.builder()
                .articleId(target.getArticleId())
                .postedDate(target.getPostedDate())
                .subject(target.getSubject())
                .category(target.getCategory().getName())
                .baseUrl(CategoryName.LIBRARY.getName().equals(target.getCategory().getName()) ? libraryBaseUrl : normalBaseUrl)
                .build();
    }
}
