package com.kustacks.kuring.worker.updater.notice.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
public class KuisNoticeResponseDTO extends NoticeResponseDTO {
    @JsonProperty("DS_LIST")
    List<KuisNoticeDTO> kuisNoticeDTOList;
}