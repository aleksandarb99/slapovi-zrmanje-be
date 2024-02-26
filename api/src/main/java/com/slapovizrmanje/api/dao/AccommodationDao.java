package com.slapovizrmanje.api.dao;

import com.slapovizrmanje.api.exception.DbErrorException;
import com.slapovizrmanje.shared.mapper.AccommodationMapper;
import com.slapovizrmanje.shared.model.Accommodation;
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
public class AccommodationDao {
  private final DynamoDbTable<Accommodation> accommodationTable;
  private final AccommodationMapper accommodationMapper;
  private final String tableName = "accommodation";
  private final DynamoDbClient dynamoDbClient;

  public List<Accommodation> findByEmailAndIdPair(final String email, String id) {
    final AttributeValue emailAttribute = AttributeValue.builder()
            .s(email)
            .build();
    final AttributeValue idAttribute = AttributeValue.builder()
            .s(id)
            .build();
    final QueryRequest queryRequest = QueryRequest.builder()
            .tableName(tableName)
            .keyConditionExpression("#id = :id AND #email = :email")
            .expressionAttributeNames(Map.of("#id", "id", "#email", "email"))
            .expressionAttributeValues(Map.of(":id", idAttribute, ":email", emailAttribute))
            .build();

    return executeQueryRequestAndMapItems(queryRequest);
  }

  public List<Accommodation> findByEmail(final String email) {
    final AttributeValue emailAttribute = AttributeValue.builder()
            .s(email)
            .build();
    final QueryRequest queryRequest = QueryRequest.builder()
            .tableName(tableName)
            .keyConditionExpression("#email = :email")
            .expressionAttributeNames(Map.of("#email", "email"))
            .expressionAttributeValues(Map.of(":email", emailAttribute))
            .build();

    return executeQueryRequestAndMapItems(queryRequest);
  }

  public void create(final Accommodation accommodation) {
    log.info("ACCOMMODATION TABLE - Create accommodation.");
    accommodationTable.putItem(accommodation);
  }

  public Accommodation update(final Accommodation accommodation) {
    log.info("ACCOMMODATION TABLE - Update accommodation.");
    accommodationTable.updateItem(accommodation);
    return accommodation;
  }

  private List<Accommodation> executeQueryRequestAndMapItems(QueryRequest queryRequest) {
    final QueryResponse queryResponse;
    try {
      log.info(String.format("DYNAMO DB CLIENT - Executing the query: %s", queryRequest));
      queryResponse = dynamoDbClient.query(queryRequest);
    } catch (final Exception e) {
      throw new DbErrorException(e.getMessage());
    }

    log.info("ACCOMMODATION MAPPER - Mapping to entities.");
    return queryResponse
            .items()
            .stream()
            .map(accommodationMapper::toEntity)
            .collect(Collectors.toList());
  }
}
