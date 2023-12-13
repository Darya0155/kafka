package com.arya.kafka;

import org.apache.kafka.clients.admin.TopicDescription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaAdmin;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class KafkaAdminService {

    @Autowired
    KafkaAdmin kafkaAdmin;

    public void createNewTopic(NewTopic newTopics) {
        kafkaAdmin.createOrModifyTopics(newTopics);
    }



}
