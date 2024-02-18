package com.slapovizrmanje.shared.model;

import com.slapovizrmanje.shared.model.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

//TODO: Do we need this
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    private String recordId;
    private NotificationType type;
    private String email;
    private String firstName;
    private String lastName;
    private Map<String, Integer> guests;
    private Map<String, Integer> lodging;
    private boolean powerSupply;
    private String startDate;
    private String endDate;
    private long createdAt;
    private boolean verified;
}
