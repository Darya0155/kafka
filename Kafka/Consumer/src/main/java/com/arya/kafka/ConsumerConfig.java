package com.arya.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.ObjectProvider;
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
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.ExponentialBackOff;
import org.springframework.util.backoff.FixedBackOff;


import java.util.Objects;

@Configuration
@EnableKafka
@RequiredArgsConstructor
@Slf4j
public class ConsumerConfig {

    private final KafkaProperties properties;

    private final KafkaTemplate<Integer,String> kafkaTemplate;;
    public DeadLetterPublishingRecoverer getRecover(){
        DeadLetterPublishingRecoverer deadLetterPublishingRecoverer=new DeadLetterPublishingRecoverer(kafkaTemplate,

        (c, e) -> {
            if(c.key().equals(1)){
                return new TopicPartition(c.topic()+".FAILED",c.partition());
            }else {
                return new TopicPartition(c.topic()+".RETRY",c.partition());
            }
        });
        return deadLetterPublishingRecoverer;
    }

    private DefaultErrorHandler getErrorHandler(){
        FixedBackOff fixedBackOff = new FixedBackOff(1000L, 2);


        ExponentialBackOff exponentialBackOff = new ExponentialBackOff(1000L,3);
        exponentialBackOff.setMaxAttempts(3);

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(getRecover(),exponentialBackOff);
        errorHandler.setRetryListeners((consumerRecord, e, i) -> {
            System.out.println("Attempt id "+i);
        });
        errorHandler.addNotRetryableExceptions(RuntimeException.class);
        return errorHandler;
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<?, ?> kafkaListenerContainerFactory(ConcurrentKafkaListenerContainerFactoryConfigurer configurer, ObjectProvider<ConsumerFactory<Object, Object>> kafkaConsumerFactory, ObjectProvider<ContainerCustomizer<Object, Object, ConcurrentMessageListenerContainer<Object, Object>>> kafkaContainerCustomizer, ObjectProvider<SslBundles> sslBundles) {

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
