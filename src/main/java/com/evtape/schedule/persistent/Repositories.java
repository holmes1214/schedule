package com.evtape.schedule.persistent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Repositories {

    public static UserRepository userRepository;

    @Autowired
    public void setUserRepository(UserRepository repository){
        Repositories.userRepository=repository;
    }

}
