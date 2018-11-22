package com.lhiot.healthygood.service.doctor;

import com.leon.microx.web.result.Tips;
import com.lhiot.healthygood.common.PagerResultObject;
import com.lhiot.healthygood.domain.doctor.RegisterApplication;
import com.lhiot.healthygood.domain.doctor.SettlementApplication;
import com.lhiot.healthygood.entity.AuditStatus;
import com.lhiot.healthygood.entity.SettlementStatus;
import com.lhiot.healthygood.mapper.doctor.RegisterApplicationMapper;
import com.lhiot.healthygood.mapper.user.DoctorUserMapper;
import com.lhiot.healthygood.mapper.user.FruitDoctorMapper;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Arrays;
import java.util.Date;

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
        registerApplication.setAuditStatus(AuditStatus.UNAUDITED.toString());
        registerApplication.setCreateTime(new Date());
        this.registerApplicationMapper.create(registerApplication);
        return registerApplication;
    }

}

