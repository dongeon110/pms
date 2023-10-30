package com.pms.mng.main.utils;

import lombok.Builder;
import lombok.Getter;

/**
 * 응답을 처리하기 위한 DTO 클래스
 */
@Getter
@Builder
public class ResponseDTO {
    /**
     * 응답 결과를 넣을 메세지
     */
    private String message;
}
