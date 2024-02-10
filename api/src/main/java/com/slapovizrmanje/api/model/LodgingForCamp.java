package com.slapovizrmanje.api.model;

import lombok.*;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
@Setter
public class LodgingForCamp {
  private int tent;
  private int caravan;
  private int car;
  private int sleepingBag;
  @DynamoDbAttribute("tent")
  public int getTent() {
    return tent;
  }
  @DynamoDbAttribute("caravan")
  public int getCaravan() {
    return caravan;
  }
  @DynamoDbAttribute("car")
  public int getCar() {
    return car;
  }
  @DynamoDbAttribute("sleeping_bug")
  public int getSleepingBag() {
    return sleepingBag;
  }
}
