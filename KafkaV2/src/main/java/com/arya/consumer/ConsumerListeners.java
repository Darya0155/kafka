package com.arya.consumer;

import com.arya.exceptions.DontRetryOnFailedException;
import com.arya.exceptions.RetryOnFailedException;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class ConsumerListeners {




    @KafkaListener(topics = "library-events",groupId = "test1")
    public void bookEventConsumer(ConsumerRecord<String, String> consumerRecord) {
        if(consumerRecord.key().equals("error"))
            throw new DontRetryOnFailedException("Do not retry");
        if(consumerRecord.key().equals("dlt"))
            throw new RetryOnFailedException("Please retry");

        log.info("Record Recived {} ",consumerRecord.value());
    }

    @KafkaListener(topics = "${topic.dlt}",groupId = "test2")
    public void bookEventDLT(ConsumerRecord<String, String> consumerRecord) {
        log.info( "DLT Record Recived {} ",consumerRecord.value());
    }

    @KafkaListener(topics = "${topic.error}",groupId = "test3")
    public void bookEventERROR(ConsumerRecord<String, String> consumerRecord) {
        log.info("ERROR Record Recived {} ",consumerRecord.value());
    }
}
