package com.pms.mng.main.service.impl;

import com.pms.mng.main.mapper.SampleMapper;
import com.pms.mng.main.service.SampleService;
import org.springframework.stereotype.Service;

/**
 * 샘플 서비스 구현체
 */
@Service("sampleService")
public class SampleServiceImpl implements SampleService {

    /**
     * DI
     */
    private final SampleMapper sampleMapper;
    public SampleServiceImpl(SampleMapper sampleMapper) {
        this.sampleMapper = sampleMapper;
    }

    @Override
    public Integer selectOne() {
        return sampleMapper.selectOne();
    }
}
