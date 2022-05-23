package com.kustacks.kuring.worker.updater.notice;

import com.kustacks.kuring.worker.error.ErrorCode;
import com.kustacks.kuring.worker.error.InternalLogicException;
import com.kustacks.kuring.worker.notifier.firebase.NotifierFirebaseClient;
import com.kustacks.kuring.worker.notifier.firebase.dto.NoticeFBMessageDTO;
import com.kustacks.kuring.worker.persistence.category.Category;
import com.kustacks.kuring.worker.persistence.category.CategoryRepository;
import com.kustacks.kuring.worker.persistence.notice.Notice;
import com.kustacks.kuring.worker.persistence.notice.NoticeRepository;
import com.kustacks.kuring.worker.updater.CategoryName;
import com.kustacks.kuring.worker.updater.api.notice.NoticeAPIClient;
import com.kustacks.kuring.worker.updater.api.scrap.NoticeScraper;
import com.kustacks.kuring.worker.updater.deptinfo.DeptInfo;
import com.kustacks.kuring.worker.updater.notice.dto.response.CommonNoticeFormatDTO;
import com.kustacks.kuring.worker.updater.util.converter.DTOConverter;
import com.kustacks.kuring.worker.updater.util.converter.DateConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class AllNoticeUpdater extends NoticeUpdater {

    private final Map<CategoryName, NoticeAPIClient<CommonNoticeFormatDTO, CategoryName>> noticeAPIClientMap;
    private final Map<CategoryName, DeptInfo> categoryNameDeptInfoMap;
    private final NoticeScraper scraper;
    private final NoticeRepository noticeRepository;
    private final Map<String, Category> categoryMap;

    public AllNoticeUpdater(Map<CategoryName, NoticeAPIClient<CommonNoticeFormatDTO, CategoryName>> noticeAPIClientMap,
                            Map<CategoryName, DeptInfo> categoryNameDeptInfoMap,
                            NoticeScraper noticeScraper,
                            DateConverter<String, String> yyyymmddConverter,
                            DateConverter<String, String> ymdhmsToYmdConverter,
                            NoticeRepository noticeRepository,
                            Map<String, Category> categoryMap
    ) {
        this.noticeAPIClientMap = noticeAPIClientMap;
        this.categoryNameDeptInfoMap = categoryNameDeptInfoMap;
        this.scraper = noticeScraper;
        this.yyyymmddConverter = yyyymmddConverter;
        this.ymdhmsToYmdConverter = ymdhmsToYmdConverter;
        this.noticeRepository = noticeRepository;
        this.categoryMap = categoryMap;
    }

    public void update(CategoryName categoryName, boolean isFindingNew) {

        log.info("******** {} 공지 카테고리 업데이트 시작 ********", categoryName.getKorName());

        /*
            학사, 장학, 취창업, 국제, 학생, 산학, 일반, 도서관 카테고리
            +
            각 학과 공지 업데이트
         */

        List<CommonNoticeFormatDTO> commonNoticeFormatDTOList;

        try {
            if(CategoryName.BACHELOR.equals(categoryName) ||
                    CategoryName.SCHOLARSHIP.equals(categoryName) ||
                    CategoryName.EMPLOYMENT.equals(categoryName) ||
                    CategoryName.NATIONAL.equals(categoryName) ||
                    CategoryName.STUDENT.equals(categoryName) ||
                    CategoryName.INDUSTRY_UNIV.equals(categoryName) ||
                    CategoryName.NORMAL.equals(categoryName) ||
                    CategoryName.LIBRARY.equals(categoryName)
            ) {
                commonNoticeFormatDTOList = noticeAPIClientMap.get(categoryName).request(categoryName, isFindingNew);
            } else {
                commonNoticeFormatDTOList = scraper.scrap(categoryNameDeptInfoMap.get(categoryName), isFindingNew);
            }
        } catch (InternalLogicException e) {
            log.info("{} 업데이트 오류", categoryName.getKorName());
            log.info("", e);
            return; // TODO: 더 나은 처리 필요
        }

        // 도서관 카테고리를 제외한 나머지 공지들의 날짜 포맷을 yyyymmdd로 통일
        if(!CategoryName.LIBRARY.equals(categoryName)) {
            convertPostedDateToyyyyMMdd(commonNoticeFormatDTOList, categoryName);
        }

        // 현재 년월일로부터 1년 6개월 이내의 공지들만 남기기
        try {
            commonNoticeFormatDTOList = filterNoticesByDate(commonNoticeFormatDTOList, categoryName);
        } catch (ParseException e) {
            throw new InternalLogicException(ErrorCode.CANNOT_CONVERT_DATE);
        }

        compareAndUpdateDB(commonNoticeFormatDTOList, categoryName);

        log.info("******** {} 학과 공지 업데이트 종료 ********", categoryName.getKorName());
    }

    protected void compareAndUpdateDB(List<CommonNoticeFormatDTO> commonNoticeFormatDTOList, CategoryName categoryName) {

        Category categoryEntity = categoryMap.get(categoryName.getName());

        // categoryName에 대응하는, DB에 존재하는 공지 데이터
        Map<String, Notice> dbNoticeMap = noticeRepository.findByCategoryMap(categoryEntity);

        // commonNoticeFormatDTOList를 순회하면서
        // 현재 공지가 dbNoticeList에 있고, 그 내용이 완전 동일하면 해당 공지에는 아무런 작업을 하지 않는다.
        // dbNoticeList에 없다면, 새 공지이지만 AllNoticeUpdater에서는 새 공지는 취급하지 않는다.
        // 작업이 끝난 후 dbNoticeList에 공지가 남아있다면, 해당 공지들은 DB에서 삭제한다. (실제 DB에 삭제)
        List<Notice> modifiedNotices = new LinkedList<>(); // DB에 업데이트할 공지 임시 저장
        Iterator<CommonNoticeFormatDTO> noticeIterator = commonNoticeFormatDTOList.iterator();
        while(noticeIterator.hasNext()) {
            CommonNoticeFormatDTO notice = noticeIterator.next();
            Notice dbNotice = dbNoticeMap.get(notice.getArticleId());

            // TODO: test를 위해 fullUrl과 baseUrl도 비교하도록 설정.
            // DB에 해당 공지가 존재할 때 
            // 1. 공지의 고유번호, 게시일, 제목이 모두 동일하면 아무 작업도 하지 않음
            // 2. 공지의 고유변호, 게시일, 제목 중 하나라도 다르면 업데이트 후 modifiedNotices에 임시 저장
            if(dbNotice != null) {
                dbNoticeMap.remove(notice.getArticleId());
                if(!isSame(notice, dbNotice)) {
                    dbNotice.setArticleId(notice.getArticleId());
                    dbNotice.setPostedDate(notice.getPostedDate());
                    dbNotice.setSubject(notice.getSubject());
                    dbNotice.setBaseUrl(notice.getBaseUrl());
                    dbNotice.setFullUrl(notice.getFullUrl());
                    modifiedNotices.add(dbNotice);
                }
            }
            // TODO: for local 작업
            else {
                modifiedNotices.add(Notice.builder()
                                .articleId(notice.getArticleId())
                                .postedDate(notice.getPostedDate())
                                .updatedDate(notice.getUpdatedDate())
                                .subject(notice.getSubject())
                                .baseUrl(notice.getBaseUrl())
                                .fullUrl(notice.getFullUrl())
                                .category(categoryEntity)
                                .build());
            }
        }

        // 업데이트로 인해 없어져야될 공지 삭제
        Collection<Notice> removedNotices = dbNoticeMap.values();
        noticeRepository.deleteAll(removedNotices);

        // 업데이트로 인해 내용이 변경된 공지 반영
        noticeRepository.saveAllAndFlush(modifiedNotices);

        if(!removedNotices.isEmpty()) {
            log.info("=== 업데이트 후 삭제된 공지 ===");
            for (Notice notice : removedNotices) {
                log.info("{} {} {} {}", notice.getArticleId(), notice.getPostedDate(), notice.getCategory().getName(), notice.getSubject());
            }
        }
        if(!modifiedNotices.isEmpty()) {
            log.info("=== 업데이트 후 변경된 공지 ===");
            for (Notice notice : modifiedNotices) {
                log.info("{} {} {} {}", notice.getArticleId(), notice.getPostedDate(), notice.getCategory().getName(), notice.getSubject());
            }
        }
    }

    // TODO: 여기에 이 메서드를 선언하는게 좋은 방법은 아닌거같음
    private boolean isSame(CommonNoticeFormatDTO a, Notice b) {
        return a.getArticleId().equals(b.getArticleId()) &&
                a.getPostedDate().equals(b.getPostedDate()) &&
                a.getSubject().equals(b.getSubject()) &&
                a.getBaseUrl().equals(b.getBaseUrl()) &&
                a.getFullUrl().equals(b.getFullUrl());
    }
}
