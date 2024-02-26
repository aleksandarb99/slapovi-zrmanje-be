package com.slapovizrmanje.shared.model;

import com.slapovizrmanje.shared.model.enums.AccommodationState;
import com.slapovizrmanje.shared.model.enums.AccommodationType;
import com.slapovizrmanje.shared.model.enums.Language;
import lombok.*;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

import java.time.LocalDate;
import java.util.Map;

@Setter
@Builder
@ToString
@DynamoDbBean
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Accommodation {
  private String id;
  @EqualsAndHashCode.Include
  private String email;
  @EqualsAndHashCode.Include
  private AccommodationType type;
  private AccommodationState state;
  private Language language;
  private String code;
  private String firstName;
  private String lastName;
  private Guests guests;
  private Map<String, Integer> lodging;
  private boolean powerSupply;
  @EqualsAndHashCode.Include
  private LocalDate startDate;
  @EqualsAndHashCode.Include
  private LocalDate endDate;
  private long createdAt;

  @DynamoDbPartitionKey()
  public String getEmail() {
    return email;
  }

  @DynamoDbSortKey()
  public String getId() {
    return id;
  }

  @DynamoDbAttribute("code")
  public String getCode() {
    return code;
  }

  @DynamoDbAttribute("type")
  public AccommodationType getType() {
    return type;
  }

  @DynamoDbAttribute("state")
  public AccommodationState getState() {
    return state;
  }

  @DynamoDbAttribute("language")
  public Language getLanguage() {
    return language;
  }

  @DynamoDbAttribute("first_name")
  public String getFirstName() {
    return firstName;
  }

  @DynamoDbAttribute("last_name")
  public String getLastName() {
    return lastName;
  }

  @DynamoDbAttribute("guests")
  public Guests getGuests() {
    return guests;
  }

  @DynamoDbAttribute("lodging")
  public Map<String, Integer> getLodging() {
    return lodging;
  }

  @DynamoDbAttribute("power_supply")
  public boolean isPowerSupply() {
    return powerSupply;
  }

  @DynamoDbAttribute("start_date")
  public LocalDate getStartDate() {
    return startDate;
  }

  @DynamoDbAttribute("end_date")
  public LocalDate getEndDate() {
    return endDate;
  }

  @DynamoDbAttribute("created_at")
  public long getCreatedAt() {
    return createdAt;
  }
}
