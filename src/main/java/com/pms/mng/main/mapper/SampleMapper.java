package com.pms.mng.main.mapper;

import org.apache.ibatis.annotations.Mapper;

/**
 * 샘플 Mapper Interface
 */
@Mapper
public interface SampleMapper {

    /**
     * 샘플 메서드 Select 1;
     * @return 1
     */
    Integer selectOne();
}
