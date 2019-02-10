package com.lhiot.healthygood.service.customplan;

import com.leon.microx.web.result.Pages;
import com.leon.microx.web.result.Tips;
import com.lhiot.healthygood.domain.customplan.CustomPlanSpecification;
import com.lhiot.healthygood.domain.customplan.CustomPlanSpecificationStandard;
import com.lhiot.healthygood.domain.customplan.model.CustomPlanSpecificationStandardParam;
import com.lhiot.healthygood.mapper.customplan.CustomPlanSpecificationMapper;
import com.lhiot.healthygood.mapper.customplan.CustomPlanSpecificationStandardMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Description:定制计划规格基础数据服务类
 *
 * @author hufan
 * @date 2018/12/08
 */
@Service
@Transactional
public class CustomPlanSpecificationStandardService {

    private final CustomPlanSpecificationStandardMapper customPlanSpecificationStandardMapper;
    private final CustomPlanSpecificationMapper customPlanSpecificationMapper;

    @Autowired
    public CustomPlanSpecificationStandardService(CustomPlanSpecificationStandardMapper customPlanSpecificationStandardMapper, CustomPlanSpecificationMapper customPlanSpecificationMapper) {
        this.customPlanSpecificationStandardMapper = customPlanSpecificationStandardMapper;
        this.customPlanSpecificationMapper = customPlanSpecificationMapper;
    }

    /**
     * Description:新增定制计划规格基础数据
     *
     * @param customPlanSpecificationStandard
     * @return
     * @author hufan
     * @date 2018/12/08 09:07:00
     */
    public Long create(CustomPlanSpecificationStandard customPlanSpecificationStandard) {
        this.customPlanSpecificationStandardMapper.create(customPlanSpecificationStandard);
        return customPlanSpecificationStandard.getId();
    }

    /**
     * Description:根据id修改定制计划规格基础数据
     *
     * @param customPlanSpecificationStandard
     * @return
     * @author hufan
     * @date 2018/12/08 09:07:00
     */
    public Tips updateById(Long id, CustomPlanSpecificationStandard customPlanSpecificationStandard) {
        customPlanSpecificationStandard.setId(id);
        boolean updateStandard = this.customPlanSpecificationStandardMapper.updateById(customPlanSpecificationStandard) > 0;
        if (!updateStandard) {
            return Tips.warn("修改定制计划规格基础数据失败！");
        }
        List<CustomPlanSpecification> customPlanSpecificationList = customPlanSpecificationMapper.selectByStandardsIds(Arrays.asList(id.toString()));
        if (!CollectionUtils.isEmpty(customPlanSpecificationList)) {
            // 批量修改定制规格的配图
            CustomPlanSpecification customPlanSpecification = new CustomPlanSpecification();
            customPlanSpecification.setImage(customPlanSpecificationStandard.getImage());
            customPlanSpecification.setDescription(customPlanSpecificationStandard.getDescription());
            customPlanSpecification.setQuantity(customPlanSpecificationStandard.getQuantity());
            customPlanSpecification.setStandardId(id);
            boolean updateSpecification = this.customPlanSpecificationMapper.updateByStandardId(customPlanSpecification) > 0;
            if (!updateSpecification) {
                return Tips.warn("修改定制计划规格失败!");
            }
        }
        return Tips.empty();
    }

    /**
     * Description:根据ids删除定制计划规格基础数据
     *
     * @param ids
     * @return
     * @author hufan
     * @date 2018/12/08 09:07:00
     */
    public boolean deleteByIds(String ids) {
        return this.customPlanSpecificationStandardMapper.deleteByIds(Arrays.asList(ids.split(","))) > 0;
    }

    /**
     * Description:根据id查找定制计划规格基础数据
     *
     * @param id
     * @return
     * @author hufan
     * @date 2018/12/08 09:07:00
     */
    public CustomPlanSpecificationStandard selectById(Long id) {
        return this.customPlanSpecificationStandardMapper.selectById(id);
    }

    /**
     * Description: 查询定制计划规格基础数据总记录数
     *
     * @param customPlanSpecificationStandardParam
     * @return
     * @author hufan
     * @date 2018/12/08 09:07:00
     */
    public int count(CustomPlanSpecificationStandardParam customPlanSpecificationStandardParam) {
        return this.customPlanSpecificationStandardMapper.pageCustomPlanSpecificationStandardCounts(customPlanSpecificationStandardParam);
    }

    /**
     * Description: 查询定制计划规格基础数据分页列表
     *
     * @param customPlanSpecificationStandardParam
     * @return
     * @author hufan
     * @date 2018/12/08 09:07:00
     */
    public Pages<CustomPlanSpecificationStandard> pageList(CustomPlanSpecificationStandardParam customPlanSpecificationStandardParam) {
        int total = 0;
        if (customPlanSpecificationStandardParam.getRows() != null && customPlanSpecificationStandardParam.getRows() > 0) {
            total = this.count(customPlanSpecificationStandardParam);
        }
        return Pages.of(total,
                this.customPlanSpecificationStandardMapper.pageCustomPlanSpecificationStandards(customPlanSpecificationStandardParam));
    }

    /**
     * Description: 查询定制计划规格基础数据列表
     *
     * @param
     * @return
     * @author hufan
     * @date 2018/12/08 09:07:00
     */
    public List<CustomPlanSpecificationStandard> findList(){

        return this.customPlanSpecificationStandardMapper.findList();
    }
}

