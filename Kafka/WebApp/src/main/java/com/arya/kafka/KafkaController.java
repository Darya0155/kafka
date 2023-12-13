package com.arya.kafka;

import com.arya.kafka.event.LibraryEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;

@RestController
@Slf4j
@RequiredArgsConstructor
public class KafkaController
{
    @Value("${spring.kafka.topic}")
    private String topic;

    private final kafkaProducerService<Integer,String> kafkaProducerService;

    private final ObjectMapper objectMapper;

    @PostMapping("v1/sendLibraryEvent")
    public ResponseEntity<LibraryEvent> sendLibraryEventV1(@RequestBody LibraryEvent event) throws JsonProcessingException {
        log.info("messge {}", event);


        String eventAsString = objectMapper.writeValueAsString(event);

        var headers=new HashMap<String,String>(){{
                put("timeStamp",String.valueOf(System.currentTimeMillis()));
            }
        };

        kafkaProducerService.send(topic,event.getId(),eventAsString,headers);

        return ResponseEntity.status(HttpStatus.CREATED).body(event);
    }

}
