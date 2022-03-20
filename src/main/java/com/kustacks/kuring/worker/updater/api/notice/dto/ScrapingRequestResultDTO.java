package com.kustacks.kuring.worker.updater.api.notice.dto;

import lombok.Builder;
import lombok.Getter;
import org.jsoup.nodes.Document;

@Getter
@Builder
public class ScrapingRequestResultDTO {
    private Document document;
    private String url;
}
