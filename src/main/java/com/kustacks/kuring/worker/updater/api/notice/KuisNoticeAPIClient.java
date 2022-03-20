package com.kustacks.kuring.worker.updater.api.notice;

import com.kustacks.kuring.worker.error.ErrorCode;
import com.kustacks.kuring.worker.error.InternalLogicException;
import com.kustacks.kuring.worker.updater.CategoryName;
import com.kustacks.kuring.worker.updater.notice.dto.request.*;
import com.kustacks.kuring.worker.updater.notice.dto.response.CommonNoticeFormatDTO;
import com.kustacks.kuring.worker.updater.notice.dto.response.KuisNoticeDTO;
import com.kustacks.kuring.worker.updater.notice.dto.response.KuisNoticeResponseDTO;
import com.kustacks.kuring.worker.updater.util.converter.DTOConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class KuisNoticeAPIClient implements NoticeAPIClient<CommonNoticeFormatDTO, CategoryName> {

    @Value("${notice.referer}")
    private String noticeReferer;

    @Value("${notice.request-url}")
    private String noticeUrl;

    private final RestTemplate restTemplate;
    private final DTOConverter<CommonNoticeFormatDTO, KuisNoticeDTO> kuisNoticeDTOToCommonNoticeFormatDTOConverter;
    private final KuisAuthManager kuisAuthManager;
    private final Map<CategoryName, KuisNoticeRequestBody> noticeRequestBodies;

    public KuisNoticeAPIClient(KuisAuthManager parsingKuisAuthManager,
                               DTOConverter<CommonNoticeFormatDTO, KuisNoticeDTO> kuisNoticeDTOToCommonNoticeFormatDTOConverter,

                               BachelorKuisNoticeRequestBody bachelorNoticeRequestBody,
                               ScholarshipKuisNoticeRequestBody scholarshipNoticeRequestBody,
                               EmploymentKuisNoticeRequestBody employmentKuisNoticeRequestBody,
                               NationalKuisNoticeRequestBody nationalKuisNoticeRequestBody,
                               StudentKuisNoticeRequestBody studentKuisNoticeRequestBody,
                               IndustryUnivKuisNoticeRequestBody industryUnivKuisNoticeRequestBody,
                               NormalKuisNoticeRequestBody normalKuisNoticeRequestBody,

                               RestTemplate restTemplate) {

        this.kuisNoticeDTOToCommonNoticeFormatDTOConverter = kuisNoticeDTOToCommonNoticeFormatDTOConverter;
        this.kuisAuthManager = parsingKuisAuthManager;

        this.restTemplate = restTemplate;

        noticeRequestBodies = new HashMap<>();
        noticeRequestBodies.put(CategoryName.BACHELOR, bachelorNoticeRequestBody);
        noticeRequestBodies.put(CategoryName.SCHOLARSHIP, scholarshipNoticeRequestBody);
        noticeRequestBodies.put(CategoryName.EMPLOYMENT, employmentKuisNoticeRequestBody);
        noticeRequestBodies.put(CategoryName.NATIONAL, nationalKuisNoticeRequestBody);
        noticeRequestBodies.put(CategoryName.STUDENT, studentKuisNoticeRequestBody);
        noticeRequestBodies.put(CategoryName.INDUSTRY_UNIV, industryUnivKuisNoticeRequestBody);
        noticeRequestBodies.put(CategoryName.NORMAL, normalKuisNoticeRequestBody);
    }

    @Override
    public List<CommonNoticeFormatDTO> request(CategoryName categoryName, boolean isFindingNew) throws InternalLogicException {

        // sessionId 획득
        String sessionId = kuisAuthManager.getSessionId();

        // 공지 요청 헤더
        HttpHeaders noticeRequestHeader = createKuisNoticeRequestHeader(sessionId);

        // 공지 요청
        KuisNoticeRequestBody kuisNoticeRequestBody = noticeRequestBodies.get(categoryName);

        String encodedNoticeRequestBody = KuisRequestBody.toUrlEncodedString(kuisNoticeRequestBody);
        HttpEntity<String> noticeRequestEntity = new HttpEntity<>(encodedNoticeRequestBody, noticeRequestHeader);
        ResponseEntity<KuisNoticeResponseDTO> noticeResponse;
        try {
            noticeResponse = restTemplate.exchange(noticeUrl, HttpMethod.POST, noticeRequestEntity, KuisNoticeResponseDTO.class);
        } catch(RestClientException e) {
            log.warn("세션 갱신이 필요합니다.");
            kuisAuthManager.forceRenewing();
            throw new InternalLogicException(ErrorCode.KU_LOGIN_BAD_RESPONSE, e);
        }

        KuisNoticeResponseDTO body = noticeResponse.getBody();
        if(body == null) {
            kuisAuthManager.forceRenewing();
            throw new InternalLogicException(ErrorCode.KU_NOTICE_CANNOT_PARSE_JSON);
        }

        List<KuisNoticeDTO> kuisNoticeDTOList = body.getKuisNoticeDTOList();
        if(kuisNoticeDTOList == null) {
            kuisAuthManager.forceRenewing();
            throw new InternalLogicException(ErrorCode.KU_NOTICE_CANNOT_PARSE_JSON);
        } else {
            return convertToCommonFormatDTO(kuisNoticeDTOList);
        }
    }

    private HttpHeaders createKuisNoticeRequestHeader(String sessionId) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Referer", noticeReferer);
        httpHeaders.add("Cookie", sessionId);
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        return httpHeaders;
    }

    private List<CommonNoticeFormatDTO> convertToCommonFormatDTO(List<KuisNoticeDTO> kuisNoticeDTOList) {

        List<CommonNoticeFormatDTO> converted = new LinkedList<>();
        for (KuisNoticeDTO kuisNoticeDTO : kuisNoticeDTOList) {
            converted.add(kuisNoticeDTOToCommonNoticeFormatDTOConverter.convert(kuisNoticeDTO));
        }

        return converted;
    }
}
