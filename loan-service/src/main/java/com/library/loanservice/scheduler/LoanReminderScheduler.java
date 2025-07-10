package com.library.loanservice.scheduler;

public interface LoanReminderScheduler {
    void sendLoanReminders();
    void processOverdueLoans();
}
