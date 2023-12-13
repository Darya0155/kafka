package com.arya.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ConsumerListeners {

    @KafkaListener(topics = "library-events")
    public void libraryEventConsumer(ConsumerRecord<Integer, String> consumerRecord) throws Exception {
        if(consumerRecord.key()==1){
           throw new RuntimeException("");
        }else if (consumerRecord.key()==2){
            throw new Exception("");
        }
        log.info("Record Recived {} ",consumerRecord);
    }

}
