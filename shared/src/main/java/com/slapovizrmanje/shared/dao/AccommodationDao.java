package com.slapovizrmanje.shared.dao;

import com.slapovizrmanje.shared.exception.DbErrorException;
import com.slapovizrmanje.shared.mapper.AccommodationMapper;
import com.slapovizrmanje.shared.model.Accommodation;
import com.slapovizrmanje.shared.model.enums.AccommodationState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
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

  public List<Accommodation> findWhereLastModifiedIsTodayAndStateIsNotAvailable() {
    final AttributeValue stateAttributeValue = AttributeValue.builder()
            .s(AccommodationState.NOT_AVAILABLE.toString())
            .build();
    final AttributeValue todayAttributeValue = AttributeValue.builder()
            .s(LocalDate.now().toString())
            .build();

    final var scanRequest = ScanRequest.builder()
            .tableName(tableName)
            .filterExpression("#state = :state and #last_modified = :last_modified")
            .expressionAttributeNames(Map.of("#state", "state", "#last_modified", "last_modified"))
            .expressionAttributeValues(Map.of(":state", stateAttributeValue, ":last_modified", todayAttributeValue))
            .build();

    final ScanResponse response = dynamoDbClient.scan(scanRequest);
    return response.items()
            .stream()
            .map(accommodationMapper::toEntity)
            .collect(Collectors.toList());
  }
  public List<Accommodation> findWhereStartDateIsTomorrowAndStateIsReserved() {
    final AttributeValue stateAttributeValue = AttributeValue.builder()
            .s(AccommodationState.RESERVED.toString())
            .build();
    final AttributeValue tomorrowAttributeValue = AttributeValue.builder()
            .s(LocalDate.now().plus(1, ChronoUnit.DAYS).toString())
            .build();

    final var scanRequest = ScanRequest.builder()
            .tableName(tableName)
            .filterExpression("#state = :state and #startdate = :startdate")
            .expressionAttributeNames(Map.of("#state", "state", "#startdate", "start_date"))
            .expressionAttributeValues(Map.of(":state", stateAttributeValue, ":startdate", tomorrowAttributeValue))
            .build();

    final ScanResponse response = dynamoDbClient.scan(scanRequest);
    return response.items()
            .stream()
            .map(accommodationMapper::toEntity)
            .collect(Collectors.toList());
  }
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
