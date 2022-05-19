package com.kustacks.kuring.worker.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    /**
     * ErrorCodes about InternalLogicException
     */
    KU_LOGIN_CANNOT_LOGIN("kuis 로그인 요청이 실패했습니다."),
    KU_LOGIN_NO_RESPONSE_BODY("kuis 로그인 요청에 대한 응답 body를 찾을 수 없습니다."),
    KU_LOGIN_BAD_RESPONSE("kuis 로그인 요청에 대한 응답이 비정상적입니다."),
    KU_LOGIN_NO_COOKIE_HEADER("kuis 로그인 요청에 대한 응답에 Set-Cookie 헤더가 없습니다."),
    KU_LOGIN_EMPTY_COOKIE("kuis 로그인 요청에 대한 응답 중 Set-Cookie 헤더가 비어 있습니다."),
    KU_LOGIN_NO_JSESSION("kuis 로그인 요청에 대한 응답에 Set-Cookie 헤더값이 있지만, JSESSIONID가 없습니다."),
    KU_LOGIN_CANNOT_GET_API_SKELETON("ku-boost에서 제공하는 api skeleton을 가져올 수 없습니다."),
    KU_LOGIN_CANNOT_PARSE_API_SKELETON("ku-boost에서 제공하는 api skeleton을 파싱에서 오류가 발생했습니다."),
    KU_LOGIN_IMPOSSIBLE("로그인 요청 방식이 변경된 듯 합니다. 관리자의 확인이 필요합니다."),

    KU_NOTICE_CANNOT_PARSE_JSON("kuis 공지를 POJO로 변환할 수 없습니다."),

    LIB_BAD_RESPONSE("도서관 공지 요청에 대한 응답이 비정상적입니다."),
    LIB_CANNOT_PARSE_JSON("도서관 공지를 POJO로 변환할 수 없습니다."),

    CAT_NOT_EXIST_CATEGORY("서버에서 지원하지 않는 카테고리입니다."),

//    STAFF_SCRAPER_TAG_NOT_EXIST("Jsoup - 찾고자 하는 태그가 존재하지 않습니다."),
    STAFF_SCRAPER_EXCEED_RETRY_LIMIT("교직원 업데이트 재시도 횟수를 초과했습니다."),
    STAFF_SCRAPER_CANNOT_SCRAP("건국대학교 홈페이지가 불안정합니다. 교직원 정보를 가져올 수 없습니다."),
    STAFF_SCRAPER_CANNOT_PARSE("교직원 페이지 HTML 파싱에 실패했습니다."),

    NOTICE_SCRAPER_CANNOT_SCRAP("학과 홈페이지가 불안정합니다. 공지 정보를 가져올 수 없습니다."),
    NOTICE_SCRAPER_CANNOT_PARSE("공지 페이지 HTML 파싱에 실패했습니다."),

    FB_FAIL_SUBSCRIBE("카테고리 구독에 실패했습니다."),
    FB_FAIL_UNSUBSCRIBE("카테고리 구독 해제에 실패했습니다."),
    FB_FAIL_ROLLBACK("카테고리 편집 중 transaction fail이 발생했고, 이를 복구하는데 실패했습니다."),
    FB_FAIL_SEND("FCM 메세지 전송에 실패했습니다."),

    MQ_CANNOT_CONVERT_OBJECT_TO_STRING("NewNoticeMQMessageDTO를 String으로 변환하는데 실패했습니다."),
    MQ_CANNOT_PUBLISH("MQ메세지를 전송하는데 실패했습니다."),
    MQ_CANNOT_CONNECT("MQ서버와의 커넥션 생성에 실패했습니다."),

    DB_SQLEXCEPTION("DB 작업에 문제가 발생했습니다."),

    CANNOT_CONVERT_DATE("날짜 형식 변환에 문제가 발생했습니다."),

    AD_UNAUTHENTICATED("관리자가 아닙니다."),

    UNKNOWN("알 수 없는 오류입니다.");

    private final HttpStatus httpStatus;
    private final String message;

    ErrorCode(String message) {
        this.httpStatus = null;
        this.message = message;
    }
}
