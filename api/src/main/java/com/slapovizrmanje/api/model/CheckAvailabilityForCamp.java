package com.slapovizrmanje.api.model;

import lombok.*;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

import java.time.LocalDate;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
@Setter
@EqualsAndHashCode
@ToString
public class CheckAvailabilityForCamp {
  private String id;
  private String email;
  private String firstName;
  private String lastName;
  private GuestsForCamp guests;
  private LodgingForCamp lodging;
  private boolean powerSupply;
  private LocalDate startDate;
  private LocalDate endDate;
  private boolean isVerified;
  private long createdAt;

  @DynamoDbPartitionKey()
  public String getEmail() {
    return email;
  }
  @DynamoDbSortKey()
  public String getId() {
    return id;
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
  public GuestsForCamp getGuests() {
    return guests;
  }

  @DynamoDbAttribute("lodging")
  public LodgingForCamp getLodging() {
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

  @DynamoDbAttribute("verified")
  public boolean isVerified() {
    return isVerified;
  }

  @DynamoDbAttribute("created_at")
  public long getCreatedAt() {
    return createdAt;
  }
}
