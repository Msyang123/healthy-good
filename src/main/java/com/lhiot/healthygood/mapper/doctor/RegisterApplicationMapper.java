package com.lhiot.healthygood.mapper.doctor;

import com.lhiot.healthygood.domain.doctor.RegisterApplication;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Description:鲜果师申请记录Mapper类
 *
 * @author yijun
 * @date 2018/07/26
 */
@Mapper
@Repository
public interface RegisterApplicationMapper {

    /**
     * Description:新增鲜果师申请记录
     *
     * @param registerApplication
     * @return
     * @author yijun
     * @date 2018/07/26 12:08:13
     */
    int create(RegisterApplication registerApplication);

    /**
     * Description:根据id修改鲜果师申请记录
     *
     * @param registerApplication
     * @return
     * @author yijun
     * @date 2018/07/26 12:08:13
     */
    int updateById(RegisterApplication registerApplication);

    /**
     * Description:根据ids删除鲜果师申请记录
     *
     * @param ids
     * @return
     * @author yijun
     * @date 2018/07/26 12:08:13
     */
    int deleteByIds(List<String> ids);

    /**
     * Description:根据id查找鲜果师申请记录
     *
     * @param id
     * @return
     * @author yijun
     * @date 2018/07/26 12:08:13
     */
    RegisterApplication selectById(Long id);


    /**
     * Description:根据用户id查找鲜果师最新申请记录
     *
     * @param id
     * @return
     * @author yijun
     * @date 2018/07/26 12:08:13
     */
    RegisterApplication findLastApplicationById(Long id);

    /**
     * Description:查询鲜果师申请记录列表
     *
     * @param registerApplication
     * @return
     * @author hufan
     * @date 2018/11/26 18:56:50
     */
    List<RegisterApplication> findList(RegisterApplication registerApplication);


    /**
     * Description: 查询鲜果师申请记录总记录数
     *
     * @param registerApplication
     * @return
     * @author hufan
     * @date 2018/11/26 18:56:50
     */
    int findCount(RegisterApplication registerApplication);
}
