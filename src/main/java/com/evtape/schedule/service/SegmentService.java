package com.evtape.schedule.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by holmes1214 on 18/12/2017.
 */
@Component
public class SegmentService {
    @Autowired
    private RedisService redis;

    private Logger logger = LoggerFactory.getLogger(SegmentService.class);

}
