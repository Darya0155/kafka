package com.arya.admin;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.stereotype.Service;

@Service
public class KafkaAdminService {

    @Autowired
    KafkaAdmin kafkaAdmin;

    public void createNewTopic(NewTopic newTopics) {
        kafkaAdmin.createOrModifyTopics(newTopics);
    }



}
