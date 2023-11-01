package com.pms.mng.dbdocs.web;

import com.pms.mng.dbdocs.dto.ColumnCommentDTO;
import com.pms.mng.dbdocs.dto.TableCommentDTO;
import com.pms.mng.dbdocs.service.DbdocsPostgresService;
import com.pms.mng.dbdocs.utils.DbdocsColumnWorkbook;
import com.pms.mng.dbdocs.utils.DbdocsTableWorkbook;
import com.pms.mng.dbdocs.vo.DbdocsSrchInfo;
import com.pms.mng.main.utils.ResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Tag(name="PostgreSQL DB 문서", description="PostgreSQL 테이블 코멘트 기반의 문서 출력")
@Slf4j
@RestController
@RequestMapping("/dbdocs")
public class DbDocsRestController {

    @Resource(name="dbdocsPostgresService")
    DbdocsPostgresService dbdocsPostgresService;

    /**
     * PostgreSQL의 테이블 목록 다운로드
     * @param schemaName    스키마 명 (default = "public")
     * @param tableName     테이블 명 (없으면 전체)
     * @param systemName    시스템 명 (없으면 제목 없음)
     * @return 테이블 목록 엑셀 파일
     */
    @Operation(summary = "테이블 목록 다운로드", description = "테이블 목록 엑셀파일 다운로드")
    @Parameter(name = "schema_name", description = "스키마 명. 필수")
    @Parameter(name = "table_name", description = "테이블 명. 없으면 전체")
    @Parameter(name = "system_name", description = "시스템 명. 없으면 제목 없음")
    @GetMapping("/download/tableList")
    public ResponseEntity<?> getDownloadTableList(
            @RequestParam(name="schema_name", defaultValue="public") String schemaName,
            @RequestParam(name="table_name", defaultValue="", required = false) String tableName,
            @RequestParam(name="system_name", defaultValue="", required = false) String systemName
    ) {
        // 엑셀 파일 명 (확장자 미 포함)
        String downloadFilename = "Table_List";

        // 검색 조건
        DbdocsSrchInfo dbdocsSrchInfo = DbdocsSrchInfo.builder()
                .schemaName(schemaName)
                .tableName(tableName)
                .build();
        log.debug(dbdocsSrchInfo.toString());

        // 조회
        List<TableCommentDTO> tableCommentDTOList = dbdocsPostgresService.selectTableCommentDTOList(dbdocsSrchInfo);
        
        // 조회된 데이터 기반 엑셀 파일 가공
        try (
                DbdocsTableWorkbook workbook = new DbdocsTableWorkbook();
                ) {
            // Create Sheet
            SXSSFSheet sheet = workbook.createSheet("테이블 목록");
            sheet.setRandomAccessWindowSize(100); // 메모리 행 100개로 제한, 초과시 Disk로 flush
            sheet.setColumnWidth(0, 32*70);
            sheet.setColumnWidth(1, 32*430);
            sheet.setColumnWidth(2, 32*430);
            sheet.setColumnWidth(3, 32*150);

            // sheet에 표 테이블 추가
            workbook.createTableListTable(sheet, tableCommentDTOList, systemName);
            
            /* 임시 파일 생성 및 삭제 */
            File tmpFile = File.createTempFile("TMP~", ".xlsx");
            try (OutputStream fos = new FileOutputStream(tmpFile)) {
                workbook.write(fos);
            }
            InputStream res = new FileInputStream(tmpFile) {
                @Override
                public void close() throws IOException {
                    super.close();
                    if(tmpFile.delete()) {
                        log.debug("tmpFile Delete Success.");
                    }
                }
            };
            /* 임시 파일 삭제 끝 */

            return ResponseEntity.status(HttpStatus.OK)
                    .contentLength(tmpFile.length())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header("Content-Disposition", String.format("attachment;filename=\"%s.xlsx\"", downloadFilename))
                    .body(new InputStreamResource(res));
        } catch (IOException e) {
            log.warn(e.getMessage());
            ResponseDTO responseDTO = ResponseDTO.builder()
                    .message("IOException")
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(responseDTO);
        } catch (Exception e) {
            log.error("Unexpected Error: " + e.getMessage());
            ResponseDTO responseDTO = ResponseDTO.builder()
                    .message("Unexpected Error")
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(responseDTO);
        }
    }



    /**
     * PostgreSQL 테이블 코멘트 조회
     * @param schemaName    조회 할 스키마 명
     * @param tableName     테이블 명, 없으면 전체
     * @param columnName    컬럼 명, 없으면 전체
     * @param systemName    시스템 명
     * @return 테이블 정의서 엑셀 파일
     */
    @Operation(summary="테이블 정의서 다운로드", description="테이블 정의서 엑셀파일 다운로드")
    @Parameter(name="schema_name", description="조회할 스키마 명. 기본 public. 필수")
    @Parameter(name="table_name", description="조회할 테이블 명. 없으면 전체")
    @Parameter(name="column_name", description="조회할 컬럼 명. 없으면 전체")
    @Parameter(name="system_name", description="입력할 시스템 명. 없으면 공백")
    @Parameter(name="write_date", description="입력할 날짜. 없으면 오늘")
    @Parameter(name="writer", description="입력할 작성자 명. 없으면 공백")
    @GetMapping(value="/download/tableDefinition")
    public ResponseEntity<?> getDownloadColumnList(
            @RequestParam(name="schema_name", defaultValue="public") String schemaName,
            @RequestParam(name="table_name", defaultValue="", required = false) String tableName,
            @RequestParam(name="column_name", defaultValue="", required = false) String columnName,
            @RequestParam(name="system_name", defaultValue="", required = false) String systemName,
            @RequestParam(name="write_date", defaultValue="", required = false) String writeDate,
            @RequestParam(name="writer", defaultValue="", required = false) String writer
    ) {
        // 초기값
        if (writeDate.equals("")) {
            Date now = new Date();
            writeDate = new SimpleDateFormat("yyyy-MM-dd").format(now);
            log.info("writeDate: {}", writeDate);
        }

        // 다운로드 되었을 떄 받을 파일 명 (확장자 미 포함)
        String downloadFilename = "Table_Definition";

        // 검색 조건
        DbdocsSrchInfo dbdocsSrchInfo = DbdocsSrchInfo.builder()
                .schemaName(schemaName)
                .tableName(tableName)
                .columnName(columnName)
                .build();
        log.debug(dbdocsSrchInfo.toString());

        // 조회
        List<ColumnCommentDTO> tableCommentList = dbdocsPostgresService.selectTableColumnCommentDTOList(dbdocsSrchInfo);

        try (
                DbdocsColumnWorkbook workbook = new DbdocsColumnWorkbook();
        ) {

            // Create Sheet
            SXSSFSheet sheet = workbook.createSheet("테이블정의서");
            sheet.setRandomAccessWindowSize(100); // 메모리 행 100개로 제한, 초과시 Disk로 flush
            sheet.setColumnWidth(0, 32*28); // 32 * 1 (1 pixel)
            sheet.setColumnWidth(1, 32*187);
            sheet.setColumnWidth(2, 32*277);
            sheet.setColumnWidth(3, 32*115);
            sheet.setColumnWidth(4, 32*70);
            sheet.setColumnWidth(5, 32*70);
            sheet.setColumnWidth(6, 32*70);
            sheet.setColumnWidth(7, 32*70);
            sheet.setColumnWidth(8, 32*97);
            sheet.setColumnWidth(9, 32*97);

            // sheet 에 표 테이블 추가
            workbook.createTableSheet(sheet, tableCommentList, systemName, writeDate, writer);

            /* 임시파일 생성 및 삭제 */
            File tmpFile = File.createTempFile("TMP~", ".xlsx");
            try (OutputStream fos = new FileOutputStream(tmpFile)) {
                workbook.write(fos);
            }
            InputStream res = new FileInputStream(tmpFile) {
                @Override
                public void close() throws IOException {
                    super.close();
                    if (tmpFile.delete()) {
                        log.info("tmpFile Delete Success.");
                    }
                }
            };
            /* 임시파일 삭제 끝 */

            return ResponseEntity.status(HttpStatus.OK)
                    .contentLength(tmpFile.length())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header("Content-Disposition", String.format("attachment;filename=\"%s.xlsx\"", downloadFilename))
                    .body(new InputStreamResource(res));

        } catch (IOException e) {
            log.warn(e.getMessage());
            ResponseDTO responseDTO = ResponseDTO.builder()
                    .message("IOException")
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(responseDTO);
        } catch (Exception e) {
            log.error("Unexpected Error: " + e.getMessage());
            ResponseDTO responseDTO = ResponseDTO.builder()
                    .message("Unexpected Error")
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(responseDTO);
        }
    }
}
