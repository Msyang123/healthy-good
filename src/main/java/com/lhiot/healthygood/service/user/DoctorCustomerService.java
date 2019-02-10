package com.lhiot.healthygood.service.user;

import com.leon.microx.util.Maps;
import com.leon.microx.util.StringUtils;
import com.leon.microx.web.result.Pages;
import com.leon.microx.web.session.Authority;
import com.leon.microx.web.session.Sessions;
import com.lhiot.healthygood.domain.user.DoctorCustomer;
import com.lhiot.healthygood.feign.ImsServiceFeign;
import com.lhiot.healthygood.feign.model.ImsOperation;
import com.lhiot.healthygood.mapper.user.DoctorCustomerMapper;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Description:鲜果师客户服务类
 *
 * @author yijun
 * @date 2018/07/26
 */
@Service
@Transactional
public class DoctorCustomerService {

    private final DoctorCustomerMapper doctorCustomerMapper;
    private Sessions session;
    private final ImsServiceFeign imsServiceFeign;


    @Autowired
    public DoctorCustomerService(ObjectProvider<Sessions> sessionsObjectProvider, DoctorCustomerMapper doctorCustomerMapper, ImsServiceFeign imsServiceFeign) {
        this.doctorCustomerMapper = doctorCustomerMapper;
        this.session = sessionsObjectProvider.getIfAvailable();
        this.imsServiceFeign = imsServiceFeign;
    }

    /**
     * Description:新增鲜果师客户
     *
     * @param DoctorCustomer
     * @return
     * @author yijun
     * @date 2018/07/26 12:08:13
     */
    /*public int create(DoctorCustomer DoctorCustomer){
        return this.DoctorCustomerMapper.create(DoctorCustomer);
    }*/

    /**
     * 绑定鲜果师
     *
     * @param request
     * @param doctorCustomer
     * @param uri
     * @return
     */
    public String createRelations(HttpServletRequest request, DoctorCustomer doctorCustomer, String uri) {
        String clientUri = null;
        Sessions.User sessionUser = session.create(request).user(Maps.of("userId", doctorCustomer.getUserId(),
                "openId", doctorCustomer.getOpenId()))
                .timeToLive(30, TimeUnit.MINUTES);
        ResponseEntity imsOperationRes = imsServiceFeign.selectAuthority();
        if (imsOperationRes.getStatusCode().isError()) {
            return null;
        }
        List<ImsOperation> imsOperations = (List<ImsOperation>) imsOperationRes.getBody();
        List<Authority> authorityList = imsOperations.stream()
                .map(op -> Authority.of(op.getAntUrl(), StringUtils.tokenizeToStringArray(op.getType(), ",")))
                .collect(Collectors.toList());
        sessionUser.authorities(authorityList);
        String sessionId = session.cache(sessionUser);
        DoctorCustomer doctorCustomerResult = this.doctorCustomerMapper.selectByUserId(doctorCustomer.getUserId());
        if (Objects.isNull(doctorCustomerResult) && Objects.nonNull(doctorCustomer.getDoctorId())) {//没有记录且鲜果师id不能为空
            this.doctorCustomerMapper.create(doctorCustomer);
        }
        //clientUri = accessToken.getOpenId() + "?sessionId=" + sessionId + "&clientUri=" + clientUri;
        clientUri = doctorCustomer.getOpenId() + "?sessionId=" + sessionId + "&clientUri=" + uri;
        return clientUri;
    }

    /**
     * Description:根据id修改鲜果师客户
     *
     * @param doctorCustomer
     * @return
     * @author yijun
     * @date 2018/07/26 12:08:13
     */
    public int updateById(DoctorCustomer doctorCustomer) {
        return this.doctorCustomerMapper.updateById(doctorCustomer);
    }

    /**
     * Description:鲜果师修改用户备注
     *
     * @param doctorCustomer
     * @return
     * @author yijun
     * @date 2018/07/26 12:08:13
     */
    public int updateRemarkName(DoctorCustomer doctorCustomer) {
        return this.doctorCustomerMapper.updateRemarkName(doctorCustomer);
    }


    /**
     * Description:根据ids删除鲜果师客户
     *
     * @param ids
     * @return
     * @author yijun
     * @date 2018/07/26 12:08:13
     */
    public int deleteByIds(String ids) {
        return this.doctorCustomerMapper.deleteByIds(Arrays.asList(ids.split(",")));
    }

    /**
     * Description:根据id查找鲜果师客户
     *
     * @param id
     * @return
     * @author yijun
     * @date 2018/07/26 12:08:13
     */
    public DoctorCustomer selectById(Long id) {
        return this.doctorCustomerMapper.selectById(id);
    }

    /**
     * Description:根据用户编号查找鲜果师客户
     *
     * @param userId
     * @return
     * @author yijun
     * @date 2018/07/26 12:08:13
     */
    public DoctorCustomer selectByUserId(Long userId) {
        return this.doctorCustomerMapper.selectByUserId(userId);
    }

    public List<DoctorCustomer> selectByDoctorId(Long doctorId) {
        return this.doctorCustomerMapper.selectByDoctorId(doctorId);
    }

    /**
     * Description: 查询鲜果师客户总记录数
     *
     * @param doctorCustomer
     * @return
     * @author yijun
     * @date 2018/07/26 12:08:13
     */
    public int count(DoctorCustomer doctorCustomer) {
        return this.doctorCustomerMapper.pageDoctorCustomerCounts(doctorCustomer);
    }

    /**
     * Description: 查询鲜果师客户分页列表
     *
     * @param doctorCustomerMapper
     * @return
     * @author yijun
     * @date 2018/07/26 12:08:13
     */
    public Pages<DoctorCustomer> pageList(DoctorCustomer doctorCustomerMapper) {
        int total = 0;
        if (doctorCustomerMapper.getRows() != null && doctorCustomerMapper.getRows() > 0) {
            total = this.count(doctorCustomerMapper);
        }
        return Pages.of(total,
                this.doctorCustomerMapper.pageDoctorCustomers(doctorCustomerMapper));
    }

    /**
     * Description: 查询鲜果师客户列表
     *
     * @param id
     * @return
     * @author yijun
     * @date 2018/07/26 12:08:13
     */
    public List<DoctorCustomer> doctorCustomers(Long id) {

        return this.doctorCustomerMapper.doctorCustomers(id);
    }
}

