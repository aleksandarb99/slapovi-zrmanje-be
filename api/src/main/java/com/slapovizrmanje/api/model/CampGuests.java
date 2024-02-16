package com.slapovizrmanje.api.model;

import lombok.*;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
@Setter
@EqualsAndHashCode
@ToString
public class CampGuests {
  private int adults;
  private int children;
  private int infants;
  private int pets;

  @DynamoDbAttribute("adults")
  public int getAdults() {
    return adults;
  }

  @DynamoDbAttribute("children")
  public int getChildren() {
    return children;
  }

  @DynamoDbAttribute("infants")
  public int getInfants() {
    return infants;
  }

  @DynamoDbAttribute("pets")
  public int getPets() {
    return pets;
  }
}
