package com.evtape.schedule.serivce;

import com.evtape.schedule.domain.LeaveDaySet;
import com.evtape.schedule.persistent.Repositories;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * Created by holmes1214 on 2018/7/9.
 */
@Service
public class DataInitService {
    @PostConstruct
    public void checkData(){
        checkLeaveDaySetData(1,1,"年假" ,"","年假");
        checkLeaveDaySetData(2,1,"假期编辑" ,"病假","病假");
        checkLeaveDaySetData(2,2,"假期编辑" ,"事假","事假");
        checkLeaveDaySetData(2,3,"假期编辑" ,"婚假","婚假");
        checkLeaveDaySetData(2,4,"假期编辑" ,"丧假","丧假");
        checkLeaveDaySetData(2,5,"假期编辑" ,"探亲假","探亲假");
        checkLeaveDaySetData(2,6,"假期编辑" ,"生育津贴假","生育津贴假");
        checkLeaveDaySetData(2,7,"假期编辑" ,"企业延长产假","企业延长产假");
        checkLeaveDaySetData(2,8,"假期编辑" ,"男方陪产假","男方陪产假");
        checkLeaveDaySetData(2,9,"假期编辑" ,"计生假","计生假");
        checkLeaveDaySetData(2,10,"假期编辑" ,"工伤假","工伤假");
        checkLeaveDaySetData(2,11,"假期编辑" ,"旷工假","旷工假");
        checkLeaveDaySetData(2,12,"假期编辑" ,"搬家假","搬家假");
        checkLeaveDaySetData(2,13,"假期编辑" ,"出差假","出差假");
        checkLeaveDaySetData(2,14,"假期编辑" ,"调休","调休");
        checkLeaveDaySetData(3,1,"班次变更" ,"","班次变更");
        checkLeaveDaySetData(4,1,"临时安排" ,"培训","临时安排");
        checkLeaveDaySetData(4,2,"临时安排" ,"演练","临时安排");
        checkLeaveDaySetData(4,3,"临时安排" ,"会议","临时安排");
        checkLeaveDaySetData(4,4,"临时安排" ,"活动","临时安排");
        checkLeaveDaySetData(4,5,"临时安排" ,"考试","临时安排");
        checkLeaveDaySetData(5,1,"旷工缺勤" ,"","旷工缺勤");
        checkLeaveDaySetData(6,1,"加班补班" ,"","加班补班");
        checkLeaveDaySetData(7,1,"替班" ,"","替班");
        checkLeaveDaySetData(8,1,"调离" ,"","调离");
        checkLeaveDaySetData(9,1,"零星假" ,"","零星假");
        checkLeaveDaySetData(10,1,"其他" ,"","其他");
        checkLeaveDaySetData(11,1,"撤销" ,"","撤销");
    }

    private void checkLeaveDaySetData(int i, int i1, String name, String subName, String desc) {
        LeaveDaySet d = Repositories.leaveDaySetRepository.findByLeaveTypeAndSubType(i, i1);
        if (d==null){
            d=new LeaveDaySet();
            d.setDescription(desc);
            d.setLeaveName(name);
            d.setSubName(subName);
            d.setLeaveType(i);
            d.setSubType(i1);
            Repositories.leaveDaySetRepository.save(d);
        }
    }
}
