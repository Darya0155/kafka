package com.arya.kafka;

import com.arya.kafka.event.LibraryEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.TopicDescription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@RestController
@Slf4j
@RequestMapping("admin")
public class KafkaAdminController {

    @Autowired
    KafkaAdminService kafkaAdminService;

    @PostMapping("v1/createNewTopic")
    public ResponseEntity<NewTopic> sendLibraryEventV1(@RequestParam String name,
            @RequestParam(required = false) Integer numPartitions,
            @RequestParam(required = false) Short replicationFactor) {
        log.info("name {} , numPartitions {} ,replicationFactor {} ", name, numPartitions, replicationFactor);

        NewTopic newTopic = TopicBuilder.name(name).replicas(Objects.nonNull(replicationFactor) ? replicationFactor : 1)
                .partitions(Objects.nonNull(numPartitions) ? numPartitions : 1).build();

        kafkaAdminService.createNewTopic(newTopic);
        return ResponseEntity.status(HttpStatus.CREATED).body(newTopic);
    }

//    @GetMapping("v1/topicDetails")
//    public ResponseEntity<Map<String, TopicDescription>> sendLibraryEventV1(@RequestParam String name) {
//        log.info("name {} " , name);
//
//        return ResponseEntity.status(HttpStatus.OK).body(kafkaAdminService.getTopicDetails(name));
//    }

}
