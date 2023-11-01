package com.pms.mng.dbdocs.service.impl;

import com.pms.mng.dbdocs.dto.ColumnCommentDTO;
import com.pms.mng.dbdocs.dto.TableCommentDTO;

import com.pms.mng.dbdocs.mapper.DbdocsPostgresMapper;
import com.pms.mng.dbdocs.service.DbdocsPostgresService;
import com.pms.mng.dbdocs.vo.DbdocsSrchInfo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("dbdocsPostgresService")
public class DbdocsPostgresServiceImpl implements DbdocsPostgresService {

    private final DbdocsPostgresMapper dbdocsPostgresMapper;
    public DbdocsPostgresServiceImpl(DbdocsPostgresMapper dbdocsPostgresMapper) {
        this.dbdocsPostgresMapper = dbdocsPostgresMapper;
    }

    @Override
    public List<TableCommentDTO> selectTableCommentDTOList(DbdocsSrchInfo dbdocsSrchInfo) {
        return dbdocsPostgresMapper.selectTableCommentDTOList(dbdocsSrchInfo);
    }

    @Override
    public List<ColumnCommentDTO> selectTableColumnCommentDTOList(DbdocsSrchInfo dbdocsSrchInfo) {
        return dbdocsPostgresMapper.selectTableColumnCommentDTOList(dbdocsSrchInfo);
    }
}

