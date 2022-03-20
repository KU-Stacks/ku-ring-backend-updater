package com.kustacks.kuring.worker.updater.util.converter;

import com.kustacks.kuring.worker.updater.notice.dto.response.CommonNoticeFormatDTO;
import com.kustacks.kuring.worker.updater.notice.dto.response.LibraryNoticeDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class LibraryNoticeDTOToCommonFormatDTOConverter implements DTOConverter<CommonNoticeFormatDTO, LibraryNoticeDTO> {

    @Value("${notice.library-base-url}")
    private String baseUrl;

    @Override
    public CommonNoticeFormatDTO convert(LibraryNoticeDTO target) {
        return CommonNoticeFormatDTO.builder()
                .articleId(target.getId())
                .postedDate(target.getDateCreated())
                .updatedDate(target.getLastUpdated())
                .subject(target.getTitle())
                .baseUrl(baseUrl)
                .fullUrl(baseUrl+"/"+target.getId())
                .build();
    }
}
