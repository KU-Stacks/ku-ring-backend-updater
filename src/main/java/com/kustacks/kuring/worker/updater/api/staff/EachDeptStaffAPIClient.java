package com.kustacks.kuring.worker.updater.api.staff;

import com.kustacks.kuring.worker.error.ErrorCode;
import com.kustacks.kuring.worker.error.InternalLogicException;
import com.kustacks.kuring.worker.updater.api.scrap.JsoupClient;
import com.kustacks.kuring.worker.updater.deptinfo.DeptInfo;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Component
public class EachDeptStaffAPIClient implements StaffAPIClient<Document, DeptInfo> {

    @Value("${staff.each-dept-url}")
    private String baseUrl;

    private final JsoupClient jsoupClient;

    public EachDeptStaffAPIClient(JsoupClient normalJsoupClient) {
        this.jsoupClient = normalJsoupClient;
    }

    @Override
    public List<Document> request(DeptInfo deptInfo) throws InternalLogicException {

        UriComponentsBuilder urlBuilder;
        String url;
        List<Document> documents = new LinkedList<>();

        for (String pfForumId : deptInfo.getStaffScrapInfo().getPfForumId()) {
            urlBuilder = UriComponentsBuilder.fromUriString(baseUrl).queryParam("pfForumId", pfForumId);
            url = urlBuilder.toUriString();

            Document document;
            try {
                document = jsoupClient.get(url, SCRAP_TIMEOUT);
            } catch(IOException e) {
                throw new InternalLogicException(ErrorCode.STAFF_SCRAPER_CANNOT_SCRAP, e);
            }

            Element pageNumHiddenInput = document.getElementById("totalPageCount");
            int totalPageNum = Integer.parseInt(pageNumHiddenInput.val());
            int pageNum = 1; // 이미 1페이지를 받아왔으므로 2페이지부터 호출하면됨

            Map<String, String> requestBody = new HashMap<>();
            while(true) {
                documents.add(document);

                if(++pageNum > totalPageNum) {
                    break;
                }

                try {
                    requestBody.put("pageNum", String.valueOf(pageNum));
                    document = jsoupClient.post(url, SCRAP_TIMEOUT, requestBody);
//                    document = Jsoup.connect(url)
//                            .data("pageNum", String.valueOf(pageNum))
//                            .post();
                } catch(IOException e) {
                    throw new InternalLogicException(ErrorCode.STAFF_SCRAPER_CANNOT_SCRAP, e);
                }
            }
        }

        return documents;
    }
}
