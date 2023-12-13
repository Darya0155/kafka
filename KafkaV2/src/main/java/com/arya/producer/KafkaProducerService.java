package com.arya.producer;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Service
@Data
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService<T, V> {

    private final KafkaTemplate<T, V> kafkaTemplate;

    public CompletableFuture<SendResult<T, V>> send(String topic, T key, V value) {
        CompletableFuture<SendResult<T, V>> kafkaSendResponse = kafkaTemplate.send(topic, key, value);
        return response(key,value,kafkaSendResponse);
    }
    public CompletableFuture<SendResult<T, V>> send(String topic, T key, V value, Map<String,String> headers) {

        ProducerRecord<T, V> producerRecord = new ProducerRecord<T, V>(topic, null, key, value, getRecordHeaders(headers));
        CompletableFuture<SendResult<T, V>> kafkaSendResponse = kafkaTemplate.send(producerRecord);

        return response(key,value,kafkaSendResponse);
    }

    private Iterable<Header> getRecordHeaders(Map<String,String> headers){
        var recordHeaders = new ArrayList<Header>();

        headers.forEach((headerKey,headerValue)->{
            recordHeaders.add(new RecordHeader(headerKey,headerValue.getBytes()));
        });
        return recordHeaders;
    }

    private  CompletableFuture<SendResult<T, V>> response(T key,V value,CompletableFuture<SendResult<T, V>> kafkaSendResponse){

        return kafkaSendResponse.whenComplete((result, error) -> {
            if (Objects.nonNull(error)) {
                handleError(key, value, error);
            } else {
                handleSuccess(key, value, result);
            }
        });
    }

    private void handleError(T key, V value, Throwable error) {

        log.error("Failed to push Message :: key {} , value {} , error {} ", key, value, error);
    }

    private void handleSuccess(T key, V value, SendResult<T, V> result) {
        log.info("Message pushed successfully key {} , value {} , result {} ", key, value, result);
    }

}
