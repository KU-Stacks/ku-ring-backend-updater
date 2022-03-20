package com.kustacks.kuring.worker.notifier.util.converter;

import com.kustacks.kuring.worker.notifier.firebase.dto.NoticeFBMessageDTO;
import com.kustacks.kuring.worker.notifier.mq.dto.NewNoticeMQMessageDTO;
import org.springframework.stereotype.Component;

@Component
public class NewNoticeMQMessageDTOToNoticeMessageDTOConverter implements DTOConverter<NoticeFBMessageDTO, NewNoticeMQMessageDTO> {

    @Override
    public NoticeFBMessageDTO convert(NewNoticeMQMessageDTO target) {
        return NoticeFBMessageDTO.builder()
                .articleId(target.getArticleId())
                .postedDate(target.getPostedDate())
                .subject(target.getSubject())
                .category(target.getCategory())
                .baseUrl(target.getBaseUrl())
                .fullUrl(target.getFullUrl())
                .build();
    }
}
