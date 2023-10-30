package com.pms.mng.main.web;

import com.pms.mng.main.service.SampleService;
import com.pms.mng.main.utils.ResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 메인 Rest Controller
 */
@Tag(name="Sample", description="샘플 묶음")
@Slf4j
@RestController
@RequestMapping("/main")
public class MainRestController {

    @Resource(name="sampleService")
    SampleService sampleService;

    /**
     * 메인 Get 요청 샘플
     * @return message: 샘플 메세지
     */
    @Operation(summary="DB 연결 샘플 테스트", description="SELECT 1")
    @GetMapping("/sample")
    public ResponseEntity<?> getSample() {
        Integer result = sampleService.selectOne();
        log.info("sample result: {}", result);

        ResponseDTO resultDTO = ResponseDTO.builder()
                .message("Result Sample")
                .build();
        return ResponseEntity.status(HttpStatus.OK)
                .body(resultDTO);
    }
}
