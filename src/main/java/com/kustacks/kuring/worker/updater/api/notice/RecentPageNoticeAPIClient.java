package com.kustacks.kuring.worker.updater.api.notice;

import com.kustacks.kuring.worker.error.ErrorCode;
import com.kustacks.kuring.worker.error.InternalLogicException;
import com.kustacks.kuring.worker.updater.api.notice.dto.ScrapingRequestResultDTO;
import com.kustacks.kuring.worker.updater.api.scrap.JsoupClient;
import com.kustacks.kuring.worker.updater.deptinfo.DeptInfo;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

@Slf4j
@Component
public class RecentPageNoticeAPIClient implements NoticeAPIClient<ScrapingRequestResultDTO, DeptInfo> {

    @Value("${notice.list.recent-url}")
    private String baseNoticeListUrl;

    @Value("${notice.view.recent-url}")
    private String baseNoticeViewUrl;

    private final JsoupClient jsoupClient;

    public RecentPageNoticeAPIClient(JsoupClient normalJsoupClient) {
        this.jsoupClient = normalJsoupClient;
    }

    @Override
    public List<ScrapingRequestResultDTO> request(DeptInfo deptInfo, boolean isFindingNew) throws InternalLogicException {
        List<ScrapingRequestResultDTO> reqResults = new LinkedList<>();
        List<String> boardSeqs = deptInfo.getNoticeScrapInfo().getBoardSeqs();
        List<String> menuSeqs = deptInfo.getNoticeScrapInfo().getMenuSeqs();
        String siteId = deptInfo.getNoticeScrapInfo().getSiteId();

        for(int i=0; i<boardSeqs.size(); ++i) {
            String boardSeq = boardSeqs.get(i);
            String menuSeq = menuSeqs.get(i);

            int pageNum = 1; // recentPage는 pageNum 인자가 1부터 시작. leagcy는 p가 0부터 시작이었음
            int curPage = -1;

            // isFindingNew가 아니면, 현재 학과 카테고리의 모든 공지 개수를 받아옴.
            // 그 외는 curPage = 12로 고정
            if(!isFindingNew) {
                try {
                    curPage = getTotalNoticeSize(siteId, boardSeq, menuSeq);
                } catch (IOException e) {
                    throw new InternalLogicException(ErrorCode.NOTICE_SCRAPER_CANNOT_SCRAP, e);
                } catch (NullPointerException | IndexOutOfBoundsException e) {
                    throw new InternalLogicException(ErrorCode.NOTICE_SCRAPER_CANNOT_PARSE, e);
                }
            } else {
                curPage = 12;
            }

            // HTML 요청
            String reqUrl = createRequestUrl(siteId, boardSeq, menuSeq, curPage, pageNum);
            String viewUrl = createViewUrl(siteId, boardSeq, menuSeq);
            try {
                Document document = jsoupClient.get(reqUrl, SCRAP_TIMEOUT);
                reqResults.add(ScrapingRequestResultDTO.builder().document(document).url(viewUrl).build());
            } catch(IOException e) {
                throw new InternalLogicException(ErrorCode.NOTICE_SCRAPER_CANNOT_SCRAP, e);
            } catch(NullPointerException | IndexOutOfBoundsException e) {
                throw new InternalLogicException(ErrorCode.NOTICE_SCRAPER_CANNOT_PARSE, e);
            }
        }

        return reqResults;
    }

    private int getTotalNoticeSize(String siteId, String boardSeq,
                                   String menuSeq)
            throws IOException, IndexOutOfBoundsException, NullPointerException {

        String url = createRequestUrl(siteId, boardSeq, menuSeq, 1, 1);
        Document document = jsoupClient.get(url, SCRAP_TIMEOUT);

        Element totalNoticeSizeElement = document.selectFirst(".pl15 > strong");
        if(totalNoticeSizeElement == null) {
            totalNoticeSizeElement = document.selectFirst(".total_count");
        }

        String totalNoticeSize = totalNoticeSizeElement.ownText();
        return Integer.parseInt(totalNoticeSize);
    }

    private String createRequestUrl(String siteId, String boardSeq, String menuSeq, int curPage, int pageNum) {
        UriComponentsBuilder urlBuilder = UriComponentsBuilder.fromUriString(baseNoticeListUrl)
                .queryParam("siteId", siteId)
                .queryParam("boardSeq", boardSeq)
                .queryParam("menuSeq", menuSeq)
                .queryParam("curPage", curPage)
                .queryParam("pageNum", pageNum);

        return urlBuilder.build().toUriString();
    }

    private String createViewUrl(String siteId, String boardSeq, String menuSeq) {
        UriComponentsBuilder urlBuilder = UriComponentsBuilder.fromUriString(baseNoticeViewUrl)
                .queryParam("siteId", siteId)
                .queryParam("boardSeq", boardSeq)
                .queryParam("menuSeq", menuSeq)
                .queryParam("seq", "");

        return urlBuilder.build().toUriString();
    }
}
