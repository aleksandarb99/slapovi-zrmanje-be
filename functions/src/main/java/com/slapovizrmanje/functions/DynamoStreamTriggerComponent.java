package com.slapovizrmanje.functions;

import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
@Slf4j
public class DynamoStreamTriggerComponent {

    public DynamoStreamTriggerComponent() {
    }

    public Function<DynamodbEvent, DynamodbEvent> handleDynamoStreamEvent() {
        return dynamodbEvent -> {
            dynamodbEvent.getRecords().forEach(record -> {
                log.info("Lambda triggered by DynamoDb Stream.");
                log.info(record.getEventID() + record.getEventName() + record.getEventSource());
            });
            return dynamodbEvent;
        };
    }
}
