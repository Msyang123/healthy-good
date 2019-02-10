package com.lhiot.healthygood.mapper.activity;

import com.lhiot.healthygood.domain.activity.ActivitySectionRelation;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.Map;

/**
 * @author hufan created in 2018/12/3 19:05
 **/
@Mapper
@Repository
public interface ActivitySectionRelationMapper {
    /**
     * 查询文章与版块关系记录
     *
     * @param map
     * @return 关系对象
     */
    ActivitySectionRelation selectRelation(Map<String, Object> map);


}