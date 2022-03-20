package com.kustacks.kuring.worker.updater.api.scrap;

import com.kustacks.kuring.worker.error.ErrorCode;
import com.kustacks.kuring.worker.error.InternalLogicException;
import com.kustacks.kuring.worker.updater.api.staff.StaffAPIClient;
import com.kustacks.kuring.worker.updater.deptinfo.DeptInfo;
import com.kustacks.kuring.worker.updater.api.scrap.parser.StaffHTMLParser;
import com.kustacks.kuring.worker.updater.staff.dto.StaffDTO;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class StaffScraper {

    private final Map<DeptInfo, StaffAPIClient<Document, DeptInfo>> deptInfoStaffAPIClientMap;
    private final Map<DeptInfo, StaffHTMLParser> deptInfoStaffHTMLParserMap;

    public StaffScraper(Map<DeptInfo, StaffAPIClient<Document, DeptInfo>> deptInfoStaffAPIClientMap,
                        Map<DeptInfo, StaffHTMLParser> deptInfoStaffHTMLParserMap) {

        this.deptInfoStaffAPIClientMap = deptInfoStaffAPIClientMap;
        this.deptInfoStaffHTMLParserMap = deptInfoStaffHTMLParserMap;
    }

    public List<StaffDTO> scrap(DeptInfo deptInfo) throws InternalLogicException {

        List<Document> documents;
        List<StaffDTO> staffDTOList = new LinkedList<>();

        log.info("{} HTML 요청", deptInfo.getDeptName());
        documents = deptInfoStaffAPIClientMap.get(deptInfo).request(deptInfo);
        log.info("{} HTML 수신", deptInfo.getDeptName());

        // 수신한 documents HTML 파싱
        List<String[]> parseResult = new LinkedList<>();
        log.info("{} HTML 파싱 시작", deptInfo.getDeptName());
        for (Document document : documents) {
            parseResult.addAll(deptInfoStaffHTMLParserMap.get(deptInfo).parse(document));
        }
        log.info("{} HTML 파싱 완료", deptInfo.getDeptName());

        // 파싱 결과를 staffDTO로 변환
        for (String[] oneStaffInfo : parseResult) {
            staffDTOList.add(StaffDTO.builder()
                    .name(oneStaffInfo[0])
                    .major(oneStaffInfo[1])
                    .lab(oneStaffInfo[2])
                    .phone(oneStaffInfo[3])
                    .email(oneStaffInfo[4])
                    .deptName(deptInfo.getDeptName())
                    .collegeName(deptInfo.getCollegeName()).build());
        }

        if(staffDTOList.size() == 0) {
            throw new InternalLogicException(ErrorCode.STAFF_SCRAPER_CANNOT_SCRAP);
        }

        return staffDTOList;
    }
}
