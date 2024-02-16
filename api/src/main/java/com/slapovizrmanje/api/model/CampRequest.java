package com.slapovizrmanje.api.model;

import lombok.*;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

import java.time.LocalDate;

@Setter
@Builder
@ToString
@DynamoDbBean
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class CampRequest {
  // TODO Check when we add other entities in the same table, if he reserved for camp, he should be able to reserve for room e.g.
  private String id;
  @EqualsAndHashCode.Include
  private String email;
  private String firstName;
  private String lastName;
  private CampGuests guests;
  private CampLodging lodging;
  private boolean powerSupply;
  @EqualsAndHashCode.Include
  private LocalDate startDate;
  @EqualsAndHashCode.Include
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
  public CampGuests getGuests() {
    return guests;
  }

  @DynamoDbAttribute("lodging")
  public CampLodging getLodging() {
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
