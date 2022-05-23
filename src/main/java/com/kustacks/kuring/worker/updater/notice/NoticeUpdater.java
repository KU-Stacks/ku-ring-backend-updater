package com.kustacks.kuring.worker.updater.notice;

import com.kustacks.kuring.worker.updater.CategoryName;
import com.kustacks.kuring.worker.updater.notice.dto.response.CommonNoticeFormatDTO;
import com.kustacks.kuring.worker.updater.util.converter.DateConverter;
import lombok.extern.slf4j.Slf4j;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public abstract class NoticeUpdater {

    protected DateConverter<String, String> yyyymmddConverter;
    protected DateConverter<String, String> ymdhmsToYmdConverter;

    public abstract void update(CategoryName categoryName, boolean isFindingNew);
    
    protected void convertPostedDateToyyyyMMdd(List<CommonNoticeFormatDTO> commonNoticeFormatDTOList, CategoryName categoryName) {

        for (CommonNoticeFormatDTO notice : commonNoticeFormatDTOList) {
            try {
                String converted = yyyymmddConverter.convert(notice.getPostedDate());
                notice.setPostedDate(converted);
            } catch(Exception e) {
                log.info("에러 발생 공지 내용");
                log.info("아이디 = {}", notice.getArticleId());
                log.info("게시일 = {}", notice.getPostedDate());
                log.info("제목 = {}", notice.getSubject());
                log.info("카테고리 = {}", categoryName.getKorName());
            }
        }
    }

    protected List<CommonNoticeFormatDTO> filterNoticesByDate(List<CommonNoticeFormatDTO> commonNoticeFormatDTOList, CategoryName categoryName) throws ParseException {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

        cal.add(Calendar.YEAR, -1);
        cal.add(Calendar.MONTH, -6);

        Date standardDate = dateFormat.parse(dateFormat.format(cal.getTime()));

        return commonNoticeFormatDTOList.stream().filter((notice) -> {
            try {
                String postedDate = notice.getPostedDate();
                if (CategoryName.LIBRARY.equals(categoryName)) {
                    postedDate = ymdhmsToYmdConverter.convert(postedDate);
                }

                Date noticeDate = dateFormat.parse(postedDate);
                return noticeDate.after(standardDate);
            } catch (ParseException e) {
                log.info("[{}] 잘못된 날짜 형식", categoryName.getKorName());
                log.info("[{}] {} {}", categoryName.getKorName(), notice.getPostedDate(), notice.getSubject());
                return false;
            }
        }).collect(Collectors.toList());
    }
}
