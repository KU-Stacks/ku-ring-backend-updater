package com.kustacks.kuring.worker.updater.api.notice;

import com.kustacks.kuring.worker.error.ErrorCode;
import com.kustacks.kuring.worker.error.InternalLogicException;
import com.kustacks.kuring.worker.updater.api.notice.dto.ScrapingRequestResultDTO;
import com.kustacks.kuring.worker.updater.api.scrap.JsoupClient;
import com.kustacks.kuring.worker.updater.deptinfo.DeptInfo;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

@Component
public class LegacyPageNoticeAPIClient implements NoticeAPIClient<ScrapingRequestResultDTO, DeptInfo> {

    @Value("${notice.list.legacy-url}")
    private String baseNoticeListUrl;

    @Value("${notice.view.legacy-url}")
    private String baseNoticeViewUrl;

    private final JsoupClient jsoupClient;

    private final int UNKNOWN_PAGE_NUM = 100000;

    public LegacyPageNoticeAPIClient(JsoupClient normalJsoupClient) {
        this.jsoupClient = normalJsoupClient;
    }

    @Override
    public List<ScrapingRequestResultDTO> request(DeptInfo deptInfo, boolean isFindingNew) throws InternalLogicException {
        List<ScrapingRequestResultDTO> reqResults = new LinkedList<>();

        for (String forumId : deptInfo.getNoticeScrapInfo().getForumIds()) {
            int totalPageNum = UNKNOWN_PAGE_NUM;
            int pageNum = 0;
            String viewUrl = createViewUrl(forumId);
            do {
                String reqUrl = createRequestUrl(forumId, pageNum);

                try {
                    Document document = jsoupClient.get(reqUrl, SCRAP_TIMEOUT);
                    reqResults.add(ScrapingRequestResultDTO.builder().document(document).url(viewUrl).build());

                    if(totalPageNum == UNKNOWN_PAGE_NUM) {
                        totalPageNum = getTotalPageNum(document);
                    }

                    // 새로운 공지 여부 확인하는 경우이므로 바로 break
                    if(isFindingNew) {
                        break;
                    }

                    ++pageNum;
                } catch(IOException e) {
                    throw new InternalLogicException(ErrorCode.NOTICE_SCRAPER_CANNOT_SCRAP, e);
                } catch(IndexOutOfBoundsException e) {
                    throw new InternalLogicException(ErrorCode.NOTICE_SCRAPER_CANNOT_PARSE, e);
                }
            } while(pageNum <= totalPageNum);
        }

        return reqResults;
    }

    private int getTotalPageNum(Document document) {
        Elements idxes = document.getElementsByAttributeValueContaining("src", "btn_list_next02.gif");
        String hrefValue = idxes.get(0).parent().attr("href");

        UriComponents hrefUri = UriComponentsBuilder.fromUriString(hrefValue).build();
        MultiValueMap<String, String> queryParams = hrefUri.getQueryParams();
        List<String> p = queryParams.get("p");

        return Integer.parseInt(p.get(0));
    }

    private String createRequestUrl(String forumId, int pageNum) {
        UriComponentsBuilder urlBuilder = UriComponentsBuilder.fromUriString(baseNoticeListUrl)
                .queryParam("forum", forumId)
                .queryParam("p", pageNum);

        return urlBuilder.toUriString();
    }

    private String createViewUrl(String forumId) {
        UriComponentsBuilder urlBuilder = UriComponentsBuilder.fromUriString(baseNoticeViewUrl)
                .queryParam("forum", forumId) // 없어도 될듯?? 확실하진 않음
                .queryParam("id", "");

        return urlBuilder.toUriString();
    }
}
