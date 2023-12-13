package com.arya.web;

import com.arya.admin.KafkaAdminService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
@Slf4j
@RequestMapping("admin")
public class KafkaAdminController {

    @Autowired
    KafkaAdminService kafkaAdminService;

    @PostMapping("v1/createNewTopic")
    public ResponseEntity<NewTopic> createNewTopic(@RequestParam String name,
            @RequestParam(required = false) Integer numPartitions,
            @RequestParam(required = false) Short replicationFactor) {
        log.info("name {} , numPartitions {} ,replicationFactor {} ", name, numPartitions, replicationFactor);

        NewTopic newTopic = TopicBuilder.name(name).replicas(Objects.nonNull(replicationFactor) ? replicationFactor : 1)
                .partitions(Objects.nonNull(numPartitions) ? numPartitions : 1).build();

        kafkaAdminService.createNewTopic(newTopic);
        return ResponseEntity.status(HttpStatus.CREATED).body(newTopic);
    }

}
