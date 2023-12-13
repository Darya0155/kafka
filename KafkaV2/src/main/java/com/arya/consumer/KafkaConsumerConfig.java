package com.arya.consumer;

import com.arya.exceptions.DontRetryOnFailedException;
import com.arya.exceptions.RetryOnFailedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.ContainerCustomizer;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.RetryListener;
import org.springframework.util.backoff.ExponentialBackOff;
import  org.springframework.kafka.listener.ConcurrentMessageListenerContainer;


import java.util.Objects;

@Configuration
@EnableKafka
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumerConfig {

    private final KafkaProperties properties;

    private final KafkaTemplate<String,String> kafkaTemplate;;

    @Value("${topic.dlt}")
    private String dltTopic;

    @Value("${topic.error}")
    private String errorTopic;

    @Value("${app.consumer.exponentialBackOff.initialInterval}")
    private Long backOfInitialInterval;

    @Value("${app.consumer.exponentialBackOff.multiplier}")
    private Short backOfMultiplier;


    @Value("${app.consumer.exponentialBackOff.maxAttempts}")
    private Integer backOfMaxAttempt;


    public DeadLetterPublishingRecoverer getRecover(){
        DeadLetterPublishingRecoverer deadLetterPublishingRecoverer=new DeadLetterPublishingRecoverer(kafkaTemplate,

        (c, e) -> {
            if(e.getCause().getMessage().equals("Do not retry")){
                return new TopicPartition(errorTopic,c.partition());
            }else {
                return new TopicPartition(dltTopic,c.partition());
            }
        });
        return deadLetterPublishingRecoverer;
    }

    private DefaultErrorHandler getErrorHandler(){

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(getRecover(),getExponentialBackOff());

        errorHandler.setRetryListeners(getRetryListener());

        errorHandler.addNotRetryableExceptions(DontRetryOnFailedException.class);
        errorHandler.addRetryableExceptions(RetryOnFailedException.class);

        return errorHandler;
    }


    private RetryListener getRetryListener(){
        return new RetryListener() {
            @Override
            public void failedDelivery(ConsumerRecord<?, ?> consumerRecord, Exception e, int i) {
                log.error("ConsumerRecord {} attemptID {} error {} ",consumerRecord.value(),i,e);
            }
        };
    }

    private ExponentialBackOff getExponentialBackOff(){
        ExponentialBackOff exponentialBackOff = new ExponentialBackOff(backOfInitialInterval,backOfMultiplier);
        exponentialBackOff.setMaxAttempts(backOfMaxAttempt);
        return exponentialBackOff;
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<?, ?> kafkaListenerContainerFactory
            (ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
             ObjectProvider<ConsumerFactory<Object, Object>> kafkaConsumerFactory, ObjectProvider<ContainerCustomizer<Object, Object, ConcurrentMessageListenerContainer<Object, Object>>> kafkaContainerCustomizer, ObjectProvider<SslBundles> sslBundles) {

        ConcurrentKafkaListenerContainerFactory<Object, Object> factory = new ConcurrentKafkaListenerContainerFactory();

        configurer.configure(factory, (ConsumerFactory)kafkaConsumerFactory.getIfAvailable(() -> {
            return new DefaultKafkaConsumerFactory(this.properties.buildConsumerProperties((SslBundles)sslBundles.getIfAvailable()));
        }));


        Objects.requireNonNull(factory);
        kafkaContainerCustomizer.ifAvailable(factory::setContainerCustomizer);

        factory.setCommonErrorHandler(getErrorHandler());
        return factory;
    }





}
