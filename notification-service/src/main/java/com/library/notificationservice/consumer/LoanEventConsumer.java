package com.library.notificationservice.consumer;

import com.library.common.event.LoanCreatedEvent;
import com.library.common.event.LoanOverdueEvent;
import com.library.common.event.LoanReminderEvent;
import com.library.common.event.LoanReturnedEvent;
import com.library.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoanEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(LoanEventConsumer.class);

    private final NotificationService notificationService;

    @KafkaListener(
            topics = "loan-events",
            groupId = "notification-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeLoanEvent(ConsumerRecord<String, Object> record) {
        Object event = record.value();
        logger.info("Received Kafka message from topic: {} with key: {}", record.topic(), record.key());
        logger.debug("Received event type: {}", event.getClass().getSimpleName());

        if (event instanceof LoanCreatedEvent) {
            LoanCreatedEvent createdEvent = (LoanCreatedEvent) event;
            logger.info("Handling LoanCreatedEvent for loan ID: {}", createdEvent.getLoanId());
            notificationService.handleLoanCreatedNotification(createdEvent);
        } else if (event instanceof LoanReturnedEvent) {
            LoanReturnedEvent returnedEvent = (LoanReturnedEvent) event;
            logger.info("Handling LoanReturnedEvent for loan ID: {}", returnedEvent.getLoanId());
            notificationService.handleLoanReturnedNotification(returnedEvent);
        } else if (event instanceof LoanReminderEvent) {
            LoanReminderEvent reminderEvent = (LoanReminderEvent) event;
            logger.info("Handling LoanReminderEvent for loan ID: {}", reminderEvent.getLoanId());
            notificationService.handleLoanReminderNotification(reminderEvent);
        } else if (event instanceof LoanOverdueEvent) {
            LoanOverdueEvent overdueEvent = (LoanOverdueEvent) event;
            logger.info("Handling LoanOverdueEvent for loan ID: {}", overdueEvent.getLoanId());
            notificationService.handleLoanOverdueNotification(overdueEvent);
        } else {
            logger.warn("Received unknown event type: {}", event.getClass().getName());
        }
    }
}