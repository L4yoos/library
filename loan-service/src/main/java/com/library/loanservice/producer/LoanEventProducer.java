package com.library.loanservice.producer;

import com.library.common.event.LoanCreatedEvent;
import com.library.common.event.LoanOverdueEvent;
import com.library.common.event.LoanReminderEvent;
import com.library.common.event.LoanReturnedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoanEventProducer {

    private static final String TOPIC_LOAN_EVENTS = "loan-events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishLoanCreatedEvent(LoanCreatedEvent event) {
        kafkaTemplate.send(TOPIC_LOAN_EVENTS, event.getLoanId().toString(), event);
    }

    public void publishLoanReturnedEvent(LoanReturnedEvent event) {
        kafkaTemplate.send(TOPIC_LOAN_EVENTS, event.getLoanId().toString(), event);
    }
    public void publishLoanReminderEvent(LoanReminderEvent event) {
        kafkaTemplate.send(TOPIC_LOAN_EVENTS, event.getLoanId().toString(), event);
    }

    public void publishLoanOverdueEvent(LoanOverdueEvent event) {
        kafkaTemplate.send(TOPIC_LOAN_EVENTS, event.getLoanId().toString(), event);
    }
}