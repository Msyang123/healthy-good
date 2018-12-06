package com.lhiot.healthygood.service.doctor;

import com.leon.microx.web.result.Pages;
import com.lhiot.healthygood.domain.doctor.RegisterApplication;
import com.lhiot.healthygood.type.AuditStatus;
import com.lhiot.healthygood.mapper.doctor.RegisterApplicationMapper;
import com.lhiot.healthygood.mapper.user.DoctorUserMapper;
import com.lhiot.healthygood.mapper.user.FruitDoctorMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
* Description:鲜果师申请记录服务类
* @author yijun
* @date 2018/07/26
*/
@Slf4j
@Service
@Transactional
public class RegisterApplicationService {

    private final RegisterApplicationMapper registerApplicationMapper;
    private final FruitDoctorMapper fruitDoctorMapper;
    private final DoctorUserMapper doctorUserMapper;

    @Autowired
    public RegisterApplicationService(RegisterApplicationMapper registerApplicationMapper, FruitDoctorMapper fruitDoctorMapper, DoctorUserMapper doctorUserMapper) {
        this.registerApplicationMapper = registerApplicationMapper;
        this.fruitDoctorMapper = fruitDoctorMapper;
        this.doctorUserMapper = doctorUserMapper;
    }

    /** 
    * Description:新增鲜果师申请记录
    *  
    * @param registerApplication
    * @return RegisterApplication
    * @author yijun
    * @date 2018/07/26 12:08:13
    */  
    public RegisterApplication create(RegisterApplication registerApplication){
        registerApplication.setAuditStatus(AuditStatus.UNAUDITED);
        registerApplication.setCreateAt(new Date());
        this.registerApplicationMapper.create(registerApplication);
        return registerApplication;
    }

    /**
     * Description:根据用户id查找鲜果师最新申请记录
     *
     * @param id
     * @return
     * @author yijun
     * @date 2018/07/26 12:08:13
     */
    public RegisterApplication findLastApplicationById(Long id){
        return this.registerApplicationMapper.findLastApplicationById(id);
    }

    /**
     * Description:根据id修改鲜果师申请记录
     *
     * @param registerApplication
     * @return
     */
    public int updateById(RegisterApplication registerApplication) {
        return this.registerApplicationMapper.updateById(registerApplication);
    }

    /**
     * Description: 查询鲜果师申请记录总记录数
     *
     * @param registerApplication
     * @return
     */
    public int count(RegisterApplication registerApplication) {
        return this.registerApplicationMapper.findCount(registerApplication);
    }

    /**
     * Description: 查询鲜果师申请记录分页列表
     *
     * @param registerApplication
     * @return
     */
    public Pages<RegisterApplication> pageList(RegisterApplication registerApplication) {
        List<RegisterApplication> list = registerApplicationMapper.findList(registerApplication);
        boolean pageFlag = Objects.nonNull(registerApplication.getPage()) && Objects.nonNull(registerApplication.getRows()) && registerApplication.getPage() > 0 && registerApplication.getRows() > 0;
        int total = pageFlag ? this.count(registerApplication) : list.size();
        return Pages.of(total, list);
    }

}

