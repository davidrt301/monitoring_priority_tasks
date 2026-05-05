package com.davidrt301.priority_tasks.service;

import com.davidrt301.priority_tasks.model.entities.Priority;

import lombok.AllArgsConstructor;

import org.springframework.stereotype.Component;
import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Component
@AllArgsConstructor
public class PriorityRuleEngine {

    private final Clock clock;

    public Priority calculatePriority(LocalDate dueDate, int complexity) {
        long daysUntilDue = ChronoUnit.DAYS.between(LocalDate.now(clock), dueDate);

        if (daysUntilDue <= 1 || complexity > 8) {
            return Priority.URGENT;
        } else if (daysUntilDue <= 3 || complexity > 5) {
            return Priority.HIGH;
        } else if (daysUntilDue <= 7) {
            return Priority.MEDIUM;
        }
        return Priority.LOW;
    }
}
