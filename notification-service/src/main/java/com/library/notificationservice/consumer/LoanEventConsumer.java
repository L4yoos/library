package com.library.notificationservice.consumer;

import com.library.notificationservice.event.LoanCreatedEvent;
import com.library.notificationservice.event.LoanOverdueEvent;
import com.library.notificationservice.event.LoanReminderEvent;
import com.library.notificationservice.event.LoanReturnedEvent;
import com.library.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoanEventConsumer {

    private final NotificationService notificationService;

    @KafkaListener(
            topics = "loan-events",
            groupId = "notification-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeLoanEvent(ConsumerRecord<String, Object> record) {
        Object event = record.value();

        if (event instanceof LoanCreatedEvent) {
            LoanCreatedEvent createdEvent = (LoanCreatedEvent) event;
            notificationService.handleLoanCreatedNotification(createdEvent);
        } else if (event instanceof LoanReturnedEvent) {
            LoanReturnedEvent returnedEvent = (LoanReturnedEvent) event;
            notificationService.handleLoanReturnedNotification(returnedEvent);
        } else if (event instanceof LoanReminderEvent) {
            LoanReminderEvent reminderEvent = (LoanReminderEvent) event;
            notificationService.handleLoanReminderNotification(reminderEvent);
        } else if (event instanceof LoanOverdueEvent) {
            LoanOverdueEvent overdueEvent = (LoanOverdueEvent) event;
            notificationService. handleLoanOverdueNotification(overdueEvent);
        }
    }
}