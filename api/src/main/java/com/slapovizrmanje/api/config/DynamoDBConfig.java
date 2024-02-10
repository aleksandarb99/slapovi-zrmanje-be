package com.slapovizrmanje.api.config;

import com.slapovizrmanje.api.model.CheckAvailabilityForCamp;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Configuration
public class DynamoDBConfig {
  @Bean
  public DynamoDbEnhancedClient amazonDynamoDB() {
    return DynamoDbEnhancedClient.builder().dynamoDbClient(dynamoDbClient())
            .build();
  }

  @Bean
  public DynamoDbClient dynamoDbClient() {
    return DynamoDbClient.builder()
            .region(Region.of("eu-central-1"))
            .build();
  }
  @Bean
  public DynamoDbTable<CheckAvailabilityForCamp> requestTable() {
    return amazonDynamoDB().table("request-table", TableSchema.fromBean(CheckAvailabilityForCamp.class));
  }
}
