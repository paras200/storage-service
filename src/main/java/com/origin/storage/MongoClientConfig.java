package com.origin.storage;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.mongodb.MongoClient;

@Configuration
@Profile("coinxdb")
public class MongoClientConfig {

    @Bean
    public MongoClient createConnection() {
    	String mongoIp = "127.0.0.1:27017";
        return new MongoClient(mongoIp);
    }
}
