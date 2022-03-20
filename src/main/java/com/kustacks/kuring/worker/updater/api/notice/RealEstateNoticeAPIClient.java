package com.kustacks.kuring.worker.updater.api.notice;

import com.kustacks.kuring.worker.error.ErrorCode;
import com.kustacks.kuring.worker.error.InternalLogicException;
import com.kustacks.kuring.worker.updater.api.notice.dto.ScrapingRequestResultDTO;
import com.kustacks.kuring.worker.updater.api.scrap.JsoupClient;
import com.kustacks.kuring.worker.updater.deptinfo.DeptInfo;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

@Component
public class RealEstateNoticeAPIClient implements NoticeAPIClient<ScrapingRequestResultDTO, DeptInfo> {

    @Value("${notice.list.real-estate-url}")
    private String baseNoticeListUrl;

    @Value("${notice.view.real-estate-url}")
    private String baseNoticeViewUrl;

    private final JsoupClient jsoupClient;
    private final int UNKNOWN_PAGE_NUM = 100000;

    public RealEstateNoticeAPIClient(JsoupClient normalJsoupClient) {
        this.jsoupClient = normalJsoupClient;
    }

    @Override
    public List<ScrapingRequestResultDTO> request(DeptInfo deptInfo, boolean isFindingNew) throws InternalLogicException {
        List<ScrapingRequestResultDTO> reqResults = new LinkedList<>();

        int totalPageNum = UNKNOWN_PAGE_NUM;
        int pageNum = 1;
        String sca = "학부";
        String viewUrl = createViewUrl(sca);

        do {
            try {
                String reqUrl = createRequestUrl(sca, pageNum);

                Document document = jsoupClient.get(reqUrl, SCRAP_TIMEOUT);
                reqResults.add(ScrapingRequestResultDTO.builder().document(document).url(viewUrl).build());

                // 첫 페이지만 필요한 경우이므로 한 번 요청 후 바로 break
                if(isFindingNew) {
                    break;
                }

                if(totalPageNum == UNKNOWN_PAGE_NUM) {
                    totalPageNum = getTotalPageNum(document);
                }

                ++pageNum;
            } catch(IOException e) {
                throw new InternalLogicException(ErrorCode.NOTICE_SCRAPER_CANNOT_SCRAP, e);
            } catch(IndexOutOfBoundsException e) {
                throw new InternalLogicException(ErrorCode.NOTICE_SCRAPER_CANNOT_PARSE, e);
            }
        } while(pageNum <= totalPageNum);

        return reqResults;
    }

    private int getTotalPageNum(Document document) {
        Element lastPageNumElement = document.select(".paging > ul > li").last();
        Element lastPageBtnElement = lastPageNumElement.getElementsByTag("a").get(1);
        String hrefValue = lastPageBtnElement.attr("href");

        UriComponents hrefUri = UriComponentsBuilder.fromUriString(hrefValue).build();
        MultiValueMap<String, String> queryParams = hrefUri.getQueryParams();
        List<String> page = queryParams.get("page");
        return Integer.parseInt(page.get(0));
    }

    private String createRequestUrl(String sca, int pageNum) {
        UriComponentsBuilder urlBuilder = UriComponentsBuilder.fromUriString(baseNoticeListUrl)
                .queryParam("sca", sca)
                .queryParam("page", pageNum);

        return urlBuilder.build().toUriString();
    }

    private String createViewUrl(String sca) {
        UriComponentsBuilder urlBuilder = UriComponentsBuilder.fromUriString(baseNoticeViewUrl)
                .queryParam("sca", sca)
                .queryParam("wr_id", "");

        return urlBuilder.build().toUriString();
    }
}
