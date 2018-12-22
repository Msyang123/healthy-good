package com.lhiot.healthygood.service.doctor;

import com.leon.microx.util.Maps;
import com.leon.microx.web.result.Pages;
import com.leon.microx.web.result.Tips;
import com.lhiot.healthygood.domain.doctor.Achievement;
import com.lhiot.healthygood.domain.doctor.DoctorAchievementLog;
import com.lhiot.healthygood.domain.doctor.IncomeStat;
import com.lhiot.healthygood.domain.doctor.TeamAchievement;
import com.lhiot.healthygood.domain.user.FruitDoctor;
import com.lhiot.healthygood.feign.type.OperationStatus;
import com.lhiot.healthygood.mapper.doctor.DoctorAchievementLogMapper;
import com.lhiot.healthygood.mapper.user.DoctorCustomerMapper;
import com.lhiot.healthygood.mapper.user.FruitDoctorMapper;
import com.lhiot.healthygood.type.BalanceType;
import com.lhiot.healthygood.type.DateTypeEnum;
import com.lhiot.healthygood.type.PeriodType;
import com.lhiot.healthygood.util.DateCalculation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
* Description:鲜果师业绩记录服务类
* @author yijun
* @date 2018/07/26
*/
@Service
@Transactional
public class DoctorAchievementLogService {

    private final DoctorAchievementLogMapper doctorAchievementLogMapper;
    private final DoctorCustomerMapper doctorCustomerMapper;
    private final FruitDoctorMapper fruitDoctorMapper;

    @Autowired
    public DoctorAchievementLogService(DoctorAchievementLogMapper doctorAchievementLogMapper,
									   DoctorCustomerMapper doctorCustomerMapper, FruitDoctorMapper fruitDoctorMapper) {
		this.doctorAchievementLogMapper = doctorAchievementLogMapper;
		this.doctorCustomerMapper = doctorCustomerMapper;
        this.fruitDoctorMapper = fruitDoctorMapper;
    }

    /** 
    * Description:新增鲜果师业绩记录
    *  
    * @param doctorAchievementLog
    * @return
    * @author yijun
    * @date 2018/07/26 12:08:13
    */  
    /*public int create(DoctorAchievementLog doctorAchievementLog){
    	//红利结算
    	String sourceType = doctorAchievementLog.getSourceType();
    	if("SETTLEMENT".equals(sourceType)){
    		return this.doctorAchievementLogMapper.create(doctorAchievementLog);
    	}
    	//非红利结算
    	Long userId = doctorAchievementLog.getUserId();
    	Long superiorDoctorId = 0L;
    	//根据用户id查询其隶属的鲜果师，以及其鲜果师的上级鲜果师
    	DoctorCustomer doctorCustomer = doctorCustomerMapper.selectById(userId);
    	Long doctorId = doctorCustomer.getDoctorId();
    	//获取该鲜果师的上级鲜果师
    	if(Objects.nonNull(doctorId) && Objects.equals(doctorId, 0L)){
    		FruitDoctor fruitDoctor = fruitDoctorMapper.selectById(doctorId);
    		superiorDoctorId = fruitDoctor.getRefereeId();
    	}
    	Timestamp currentyTime = new Timestamp(System.currentTimeMillis());
    	Map<String,Object> map = doctorAchievementLog.getCommissionMap();
    	//销售提成
    	Integer saleCommission = (Integer) map.get("saleCommission");
    	//分销提成
    	Integer fruitCommission = (Integer) map.get("fruitCommission");
    	//提成日记参数
    	DoctorAchievementLog sc = doctorAchievementLog.toDoctorBonusLog();
    	sc.setDoctorId(doctorId);
    	sc.setAmount(saleCommission);
    	sc.setCreateAt(currentyTime);
    	//分销提成日记参数
    	DoctorAchievementLog fc = doctorAchievementLog.toDoctorBonusLog();
    	fc.setDoctorId(superiorDoctorId);
    	fc.setAmount(fruitCommission);
    	fc.setCreateAt(currentyTime);
    	//设置红利收支类型
    	if(!"REFUND".equals(sourceType)){
    		sc.setSourceType("ORDER");
    		fc.setSourceType("SUB_DISTRIBUTOR");
    	}
    	//写日志
		doctorAchievementLogMapper.create(sc);
        return this.doctorAchievementLogMapper.create(fc);
    }*/

	/**
	 * 新增业绩记录
	 * @param doctorAchievementLog
	 * @return
	 */
	public int create(DoctorAchievementLog doctorAchievementLog){
    	return doctorAchievementLogMapper.create(doctorAchievementLog);
	}

    /** 
    * Description:根据id修改鲜果师业绩记录
    *  
    * @param doctorAchievementLog
    * @return
    * @author yijun
    * @date 2018/07/26 12:08:13
    */ 
    public int updateById(DoctorAchievementLog doctorAchievementLog){
        return this.doctorAchievementLogMapper.updateById(doctorAchievementLog);
    }

	/**
	 * 加减鲜果师红利余额和可结算余额
	 * @param id 鲜果师ID
	 * @param money
	 * @param balanceType
	 * @return
	 */
    public Tips updateBonus(Long id, Integer money, BalanceType balanceType){
		FruitDoctor doctor = fruitDoctorMapper.selectById(id);
		if (Objects.isNull(doctor)){
			System.out.println("鲜果师信息不存在");
			return Tips.of(-1,"鲜果师信息不存在");
		}
		boolean flag = fruitDoctorMapper.updateBouns(Maps.of("id",id,"money",money,"balanceType",balanceType.name()))>0;
		if (!flag){
			System.out.println("可结算余额或者可结算红利更新失败");
			return Tips.of(-2,"可结算余额或者可结算红利更新失败");
		}
		System.out.println("操作成功");
		return Tips.of(1,"操作成功");
	}

    /** 
    * Description:根据ids删除鲜果师业绩记录
    *  
    * @param ids
    * @return
    * @author yijun
    * @date 2018/07/26 12:08:13
    */ 
    public int deleteByIds(String ids){
        return this.doctorAchievementLogMapper.deleteByIds(Arrays.asList(ids.split(",")));
    }
    
    /** 
    * Description:根据id查找鲜果师业绩记录
    *  
    * @param id
    * @return
    * @author yijun
    * @date 2018/07/26 12:08:13
    */ 
    public DoctorAchievementLog selectById(Long id){
        return this.doctorAchievementLogMapper.selectById(id);
    }

    /** 
    * Description: 查询鲜果师业绩记录总记录数
    *  
    * @param doctorBonusLog
    * @return
    * @author yijun
    * @date 2018/07/26 12:08:13
    */  
    public int count(DoctorAchievementLog doctorBonusLog){
        return this.doctorAchievementLogMapper.pageDoctorAchievementLogCounts(doctorBonusLog);
    }
    
    /** 
    * Description: 查询鲜果师业绩记录分页列表
    *  
    * @param doctorBonusLog
    * @return
    * @author yijun
    * @date 2018/07/26 12:08:13
    */  
    public Pages<DoctorAchievementLog> pageList(DoctorAchievementLog doctorBonusLog) {
       int total = 0;
       if (doctorBonusLog.getRows() != null && doctorBonusLog.getRows() > 0) {
           total = this.count(doctorBonusLog);
       }
       return Pages.of(total,
              this.doctorAchievementLogMapper.pageDoctorAchievementLogs(doctorBonusLog));
    }

    public Integer doctorAchievementLogCounts(DoctorAchievementLog doctorBonusLog) {
    	return this.doctorAchievementLogMapper.pageDoctorAchievementLogCounts(doctorBonusLog);
	}


    /**
     * Description: 我的收入
     *
     * @param id
     * @return
     * @author yijun
     * @date 2018/07/26 12:08:13
     */
    public IncomeStat myIncome(Long id){
        Integer bouns =  this.superiorBonusOfMonth(Maps.of("doctort",id));
        IncomeStat incomeStat = this.doctorAchievementLogMapper.myIncome(id);
        incomeStat.setBonusOfHistory(incomeStat.getBonusOfHistory()+bouns);
        incomeStat.setBonusSettled(incomeStat.getBonusSettled()+bouns);
        return incomeStat;
    }

    
	/**
	 * 统计鲜果的业绩
	 * @param dateType
	 * @param periodType
	 * @param doctorId 鲜果师id
	 * @param isAll 是否加时间过滤
	 * @param statisticalIncome 是否统计收益
	 * @return
	 */
	public Achievement achievement(DateTypeEnum dateType, PeriodType periodType,
								   Long doctorId, boolean statisticalIncome, boolean isAll, Long userId){
		Achievement achievement = new Achievement();
		Map<String,String> time = this.beginAndEndTime(dateType, periodType);
		if(Objects.isNull(time)){
			return achievement;
		}
		Map<String,Object> param = Maps.of("doctorId",doctorId,
				"beginTime", null,"endTime", null,"userId",userId);
		if(!isAll){
			param.putAll(time);
		}
		//统计业绩
		achievement = doctorAchievementLogMapper.achievement(param);
		return achievement;
	}

	/**
	 * 我的团队的个人业绩
	 * @param doctorId 鲜果师id
	 * @return
	 */
	public TeamAchievement teamAchievement(Long doctorId){
	    Integer bonus = this.superiorBonusOfMonth(Maps.of("doctorId",doctorId,"currentMonth","yes"));
        TeamAchievement teamAchievement = doctorAchievementLogMapper.teamAchievement(doctorId);
        teamAchievement.setBonusOfMonth(teamAchievement.getBonusOfMonth()+bonus);//该鲜果师订单分成+鲜果师分成
		return teamAchievement;
	}

	public Integer superiorBonusOfMonth(Map<String,Object> map){
	    return doctorAchievementLogMapper.superiorBonusOfMonth(map);
    }
	
	/**
	 * 计算统计的起始时间
	 * @param dateType
	 * @param periodType
	 * @return
	 */
	public Map<String,String> beginAndEndTime(DateTypeEnum dateType,PeriodType periodType){
		Map<String,String> result = new HashMap<>();
		int dc = periodType.ordinal();
		String beginTime = null;
		String endTime = null;
		if(DateTypeEnum.DAY.equals(dateType)){
			beginTime = DateCalculation.otherDay(dc);
			endTime = beginTime;
		}else if(DateTypeEnum.WEEK.equals(dateType)){
			beginTime = DateCalculation.firstDayOfWeek(dc);
			endTime = DateCalculation.lastDayOfWeek(dc);
		}else if(DateTypeEnum.MONTH.equals(dateType)){
			beginTime = DateCalculation.firstDayOfMonth(dc);
			endTime = DateCalculation.lastDayOfMonth(dc);
		}else if(DateTypeEnum.QUARTER.equals(dateType)){
			Map<String,String> map = DateCalculation.startAndEndDayOfQuarter(periodType);
			beginTime = map.get("startDay");
			endTime = map.get("endDay");
		}else{
			return null;
		}
		result.put("beginTime", beginTime);
		result.put("endTime", endTime);
		return result;
	}
	
	/**
	 * 根据订单id查询记录
	 * @param orderId
	 * @return
	 */
	public DoctorAchievementLog findByOrderId(Long orderId){
		return doctorAchievementLogMapper.selectByOrderId(orderId);
	}

	/**
	 * 根据鲜果师ids统计一个月的红利
	 * @param ids
	 * @return
	 */
	public Integer selectFruitDoctorCommission(List<Long> ids){
		return doctorAchievementLogMapper.selectFruitDoctorCommission(ids);
	}
}

