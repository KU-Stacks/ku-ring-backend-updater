package com.kustacks.kuring.worker.updater.api.notice;

import com.kustacks.kuring.worker.error.ErrorCode;
import com.kustacks.kuring.worker.error.InternalLogicException;
import com.kustacks.kuring.worker.updater.CategoryName;
import com.kustacks.kuring.worker.updater.notice.dto.response.CommonNoticeFormatDTO;
import com.kustacks.kuring.worker.updater.notice.dto.response.LibraryNoticeDTO;
import com.kustacks.kuring.worker.updater.notice.dto.response.LibraryNoticeResponseDTO;
import com.kustacks.kuring.worker.updater.util.converter.DTOConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.LinkedList;
import java.util.List;

@Component
public class LibraryNoticeAPIClient implements NoticeAPIClient<CommonNoticeFormatDTO, CategoryName> {

    @Value("${library.request-url}")
    private String libraryUrl;

    private final DTOConverter<CommonNoticeFormatDTO, LibraryNoticeDTO> libraryNoticeDTODTOCommonNoticeFormatDTOConverter;
    private final RestTemplate restTemplate;

    public LibraryNoticeAPIClient(DTOConverter<CommonNoticeFormatDTO, LibraryNoticeDTO> libraryNoticeDTODTOCommonNoticeFormatDTOConverter,
                                  RestTemplate restTemplate) {
        this.libraryNoticeDTODTOCommonNoticeFormatDTOConverter = libraryNoticeDTODTOCommonNoticeFormatDTOConverter;
        this.restTemplate = restTemplate;
    }

    /*
        도서관 공지 갱신
     */

    @Override
    public List<CommonNoticeFormatDTO> request(CategoryName categoryName, boolean isFindingNew) throws InternalLogicException {

        int offset = 0;
        int max = 10;
        int reqIdx = 0;
        List<LibraryNoticeDTO> libraryNoticeDTOList = new LinkedList<>();
        while(reqIdx < 2) {
            String fullLibraryUrl = UriComponentsBuilder.fromUriString(libraryUrl).queryParam("offset", offset).queryParam("max", max).build().toString();
            ResponseEntity<LibraryNoticeResponseDTO> libraryResponse = restTemplate.getForEntity(fullLibraryUrl, LibraryNoticeResponseDTO.class);
            LibraryNoticeResponseDTO libraryNoticeResponseDTO = libraryResponse.getBody();
            if(libraryNoticeResponseDTO == null) {
                throw new InternalLogicException(ErrorCode.LIB_CANNOT_PARSE_JSON);
            }

            boolean isLibraryRequestSuccess = libraryNoticeResponseDTO.isSuccess();
            if(!isLibraryRequestSuccess) {
                throw new InternalLogicException(ErrorCode.LIB_BAD_RESPONSE);
            }

            offset = max;
            max = libraryNoticeResponseDTO.getData().getTotalCount() - max;

            libraryNoticeDTOList.addAll(libraryNoticeResponseDTO.getData().getList());

            // 새로운 공지 여부 확인하는 경우이므로 바로 break
            if(isFindingNew) {
                break;
            }

            ++reqIdx;
        }

        return convertToCommonFormatDTO(libraryNoticeDTOList);
    }

    private List<CommonNoticeFormatDTO> convertToCommonFormatDTO(List<LibraryNoticeDTO> libraryNoticeDTOList) {

        List<CommonNoticeFormatDTO> ret = new LinkedList<>();
        for (LibraryNoticeDTO libraryNoticeDTO : libraryNoticeDTOList) {
            ret.add(libraryNoticeDTODTOCommonNoticeFormatDTOConverter.convert(libraryNoticeDTO));
        }

        return ret;
    }
}
