package com.evtape.schedule.web;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.evtape.schedule.consts.ResponseMeta;
import com.evtape.schedule.domain.District;
import com.evtape.schedule.domain.Station;
import com.evtape.schedule.domain.vo.ResponseBundle;
import com.evtape.schedule.persistent.Repositories;

/**
 * @author ripper 站区接口,增刪改查
 */
@RestController
@RequestMapping("/district")
public class DistrictController {
    /**
     * district列表
     */
    @GetMapping
    public ResponseBundle districtList() {
        return new ResponseBundle().success(Repositories.districtRepository.findAll());
    }

    /**
     * district增
     */
    @PostMapping
    public ResponseBundle addDistrict(@RequestBody District district) {
        try {
            Repositories.districtRepository.saveAndFlush(district);
            return new ResponseBundle().success(Repositories.districtRepository.findAll());
        } catch (Exception e) {
            return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
        }
    }

    /**
     * district改
     */
    @PutMapping("/{id}")
    public ResponseBundle updateDistrict(@PathVariable("id") Integer id, @RequestBody District form) {
        try {
            District district = Repositories.districtRepository.findOne(id);
            district.setDistrictName(form.getDistrictName());
            district.setContent(form.getContent());
            Repositories.districtRepository.saveAndFlush(district);
            return new ResponseBundle().success(Repositories.districtRepository.findAll());
        } catch (Exception e) {
            return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
        }
    }

    /**
     * district删
     */
    @DeleteMapping("/{id}")
    public ResponseBundle deleteDistrict(@PathVariable("id") Integer districtId) {
        try {
            List<Station> list = Repositories.stationRepository.findByDistrictId(districtId);
            if (list != null && list.size() > 0) {
                return new ResponseBundle().failure(ResponseMeta.DISTRICT_HASSTATION);
            }
            Repositories.districtRepository.delete(districtId);
            return new ResponseBundle().success(Repositories.districtRepository.findAll());
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseBundle().failure(ResponseMeta.REQUEST_PARAM_INVALID);
        }
    }
}
