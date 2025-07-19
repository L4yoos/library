package com.library.loanservice.producer;

import com.library.common.event.LoanCreatedEvent;
import com.library.common.event.LoanOverdueEvent;
import com.library.common.event.LoanReminderEvent;
import com.library.common.event.LoanReturnedEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoanEventProducer {

    private static final Logger logger = LoggerFactory.getLogger(LoanEventProducer.class);

    private static final String TOPIC_LOAN_EVENTS = "loan-events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishLoanCreatedEvent(LoanCreatedEvent event) {
        logger.info("Publishing LoanCreatedEvent for loan ID: {}", event.getLoanId());
        kafkaTemplate.send(TOPIC_LOAN_EVENTS, event.getLoanId().toString(), event);
        logger.debug("LoanCreatedEvent for loan ID: {} sent to topic: {}", event.getLoanId(), TOPIC_LOAN_EVENTS);
    }

    public void publishLoanReturnedEvent(LoanReturnedEvent event) {
        logger.info("Publishing LoanReturnedEvent for loan ID: {}", event.getLoanId());
        kafkaTemplate.send(TOPIC_LOAN_EVENTS, event.getLoanId().toString(), event);
        logger.debug("LoanReturnedEvent for loan ID: {} sent to topic: {}", event.getLoanId(), TOPIC_LOAN_EVENTS);
    }
    public void publishLoanReminderEvent(LoanReminderEvent event) {
        logger.info("Publishing LoanReminderEvent for loan ID: {}", event.getLoanId());
        kafkaTemplate.send(TOPIC_LOAN_EVENTS, event.getLoanId().toString(), event);
        logger.debug("LoanReminderEvent for loan ID: {} sent to topic: {}", event.getLoanId(), TOPIC_LOAN_EVENTS);
    }

    public void publishLoanOverdueEvent(LoanOverdueEvent event) {
        logger.info("Publishing LoanOverdueEvent for loan ID: {}", event.getLoanId());
        kafkaTemplate.send(TOPIC_LOAN_EVENTS, event.getLoanId().toString(), event);
        logger.debug("LoanOverdueEvent for loan ID: {} sent to topic: {}", event.getLoanId(), TOPIC_LOAN_EVENTS);
    }
}