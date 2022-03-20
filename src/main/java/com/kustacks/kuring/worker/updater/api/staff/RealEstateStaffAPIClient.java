package com.kustacks.kuring.worker.updater.api.staff;

import com.kustacks.kuring.worker.error.ErrorCode;
import com.kustacks.kuring.worker.error.InternalLogicException;
import com.kustacks.kuring.worker.updater.api.scrap.JsoupClient;
import com.kustacks.kuring.worker.updater.deptinfo.DeptInfo;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

@Component
public class RealEstateStaffAPIClient implements StaffAPIClient<Document, DeptInfo> {

    @Value("${staff.real-estate-url}")
    private String baseUrl;

    private final JsoupClient jsoupClient;

    public RealEstateStaffAPIClient(JsoupClient proxyJsoupClient) {
        this.jsoupClient = proxyJsoupClient;
    }

    @Override
    public List<Document> request(DeptInfo deptInfo) throws InternalLogicException {

        UriComponentsBuilder urlBuilder = UriComponentsBuilder.fromUriString(baseUrl);
        String url = urlBuilder.toUriString();

        Document document;
        try {
            document = jsoupClient.get(url, SCRAP_TIMEOUT);
        } catch(IOException e) {
            throw new InternalLogicException(ErrorCode.STAFF_SCRAPER_CANNOT_SCRAP, e);
        }

        List<Document> documents = new LinkedList<>();
        documents.add(document);

        return documents;
    }
}
