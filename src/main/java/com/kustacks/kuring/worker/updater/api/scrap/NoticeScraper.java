package com.kustacks.kuring.worker.updater.api.scrap;

import com.kustacks.kuring.worker.error.ErrorCode;
import com.kustacks.kuring.worker.error.InternalLogicException;
import com.kustacks.kuring.worker.updater.api.notice.NoticeAPIClient;
import com.kustacks.kuring.worker.updater.api.notice.dto.ScrapingRequestResultDTO;
import com.kustacks.kuring.worker.updater.deptinfo.DeptInfo;
import com.kustacks.kuring.worker.updater.notice.dto.response.CommonNoticeFormatDTO;
import com.kustacks.kuring.worker.updater.api.scrap.parser.NoticeHTMLParser;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class NoticeScraper {

    private final Map<DeptInfo, NoticeAPIClient<ScrapingRequestResultDTO, DeptInfo>> deptInfoNoticeAPIClientMap;
    private final Map<DeptInfo, NoticeHTMLParser> deptInfoNoticeHTMLParserMap;

    public NoticeScraper(Map<DeptInfo, NoticeAPIClient<ScrapingRequestResultDTO, DeptInfo>> deptInfoNoticeAPIClientMap,
                         Map<DeptInfo, NoticeHTMLParser> deptInfoNoticeHTMLParserMap) {

        this.deptInfoNoticeAPIClientMap = deptInfoNoticeAPIClientMap;
        this.deptInfoNoticeHTMLParserMap = deptInfoNoticeHTMLParserMap;
    }

    public List<CommonNoticeFormatDTO> scrap(DeptInfo deptInfo, boolean isFindingNew) throws InternalLogicException {
        List<ScrapingRequestResultDTO> reqResults;
        List<CommonNoticeFormatDTO> noticeDTOList = new LinkedList<>();

        long start = System.currentTimeMillis();
        log.info("[{}] HTML 요청", deptInfo.getDeptName());
        reqResults = deptInfoNoticeAPIClientMap.get(deptInfo).request(deptInfo, isFindingNew);
        log.info("[{}] HTML 수신", deptInfo.getDeptName());
        long end = System.currentTimeMillis();
        log.info("[{}] 소요된 초 = {}", deptInfo.getDeptName(), (end - start) / 1000.0);

        log.info("[{}] HTML 파싱 시작", deptInfo.getDeptName());
        int noticeSize = 0;
        for (ScrapingRequestResultDTO reqResult : reqResults) {
            Document document = reqResult.getDocument();
            String viewUrl = reqResult.getUrl();

            List<String[]> parseResult = deptInfoNoticeHTMLParserMap.get(deptInfo).parse(document);

            // 파싱 결과를 commonNoticeFormatDTO로 변환
            for (String[] oneNoticeInfo : parseResult) {
                noticeDTOList.add(CommonNoticeFormatDTO.builder()
                        .articleId(oneNoticeInfo[0])
                        .postedDate(oneNoticeInfo[1])
                        .subject(oneNoticeInfo[2])
                        .baseUrl("")
                        .fullUrl(viewUrl+oneNoticeInfo[0])
                        .build());
            }

            noticeSize += parseResult.size();
        }
        log.info("[{}] HTML 파싱 완료", deptInfo.getDeptName());
        log.info("[{}] 공지 개수 = {}", deptInfo.getDeptName(), noticeSize);

        if(noticeDTOList.size() == 0) {
            throw new InternalLogicException(ErrorCode.NOTICE_SCRAPER_CANNOT_SCRAP);
        }

        return noticeDTOList;
    }
}
