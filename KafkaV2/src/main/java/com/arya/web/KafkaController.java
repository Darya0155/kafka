package com.arya.web;

import com.arya.data.BookEvent;
import com.arya.producer.KafkaProducerService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@RestController
@Slf4j
@RequiredArgsConstructor
public class KafkaController
{
    @Value("${topic.main}")
    private String topic;

    private final KafkaProducerService<String,String> kafkaProducerService;

    private final ObjectMapper objectMapper;

    @PostMapping("v1/addBook")
    public ResponseEntity<BookEvent> sendLibraryEventV1(@RequestBody BookEvent book) throws JsonProcessingException {
        log.info("messge {}", book);


        String eventAsString = objectMapper.writeValueAsString(book);

        var headers=new HashMap<String,String>(){{
                put("timeStamp",String.valueOf(System.currentTimeMillis()));
            }
        };

        kafkaProducerService.send(topic,book.getName(),eventAsString,headers);

        return ResponseEntity.status(HttpStatus.CREATED).body(book);
    }

}
