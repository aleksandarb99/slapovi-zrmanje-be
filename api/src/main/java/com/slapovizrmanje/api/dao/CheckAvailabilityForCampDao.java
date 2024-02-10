package com.slapovizrmanje.api.dao;

import com.slapovizrmanje.api.mapper.CheckAvailabilityMapMapper;
import com.slapovizrmanje.api.model.CheckAvailabilityForCamp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Slf4j
public class CheckAvailabilityForCampDao {
  private final DynamoDbTable<CheckAvailabilityForCamp> requestTable;
  private final CheckAvailabilityMapMapper checkAvailabilityMapMapper;
  private final String tableName = "request-table";
  private final DynamoDbClient dynamoDbClient;

  public List<CheckAvailabilityForCamp> findByEmail(final String email) {
    final AttributeValue emailAttribute = AttributeValue.builder()
            .s(email)
            .build();
    final var queryRequest = QueryRequest.builder()
            .tableName(tableName)
            .keyConditionExpression("#email = :email")
            .expressionAttributeNames(Map.of("#email", "email"))
            .expressionAttributeValues(Map.of(":email", emailAttribute))
            .build();
    final QueryResponse queryResponse;
    try {
      queryResponse = dynamoDbClient.query(queryRequest);
    } catch (final Exception error) {
      final var e = new RuntimeException(error.getMessage());
      log.error("findByEmail : errorMessage=", error);
      throw e;
    }
    return queryResponse.items().stream()
            .map(checkAvailabilityMapMapper::toEntity)
            .collect(Collectors.toList());
  }

//  public List<CheckAvailabilityForCamp> findById(final String id) {
//    return groupTable.getItem(Key.builder()
//                    .sortValue(id)
//            .build());
//  }
//  public List<CheckAvailabilityForCamp> findByEmail(final String email) {
//    return groupTable.getItem(Key.builder()
//            .partitionValue(email)
//            .build());
//  }
  public CheckAvailabilityForCamp create(
          final CheckAvailabilityForCamp checkAvailabilityForCamp) {
    requestTable.putItem(checkAvailabilityForCamp);
    return checkAvailabilityForCamp;
  }
  public CheckAvailabilityForCamp update(final CheckAvailabilityForCamp checkAvailabilityForCamp) {
    requestTable.putItem(checkAvailabilityForCamp);
    return checkAvailabilityForCamp;
  }
}
