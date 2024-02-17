package com.slapovizrmanje.shared.dao;

import com.slapovizrmanje.shared.exception.DbErrorException;
import com.slapovizrmanje.shared.mapper.CampRequestMapper;
import com.slapovizrmanje.shared.model.CampRequest;
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
public class CampRequestDao {
  private final DynamoDbTable<CampRequest> campRequestTable;
  private final CampRequestMapper campRequestMapper;
  private final String tableName = "request-table";
  private final DynamoDbClient dynamoDbClient;

  public List<CampRequest> findByEmailAndIdPair(final String email, String id) {
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
  public List<CampRequest> findByEmail(final String email) {
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

  public void create(final CampRequest campRequest) {
    log.info("CAMP REQUEST TABLE - Create camp request.");
    campRequestTable.putItem(campRequest);
  }

  public CampRequest update(final CampRequest campRequest) {
    log.info("CAMP REQUEST TABLE - Update camp request.");
    campRequestTable.updateItem(campRequest);
    return campRequest;
  }

  private List<CampRequest> executeQueryRequestAndMapItems(QueryRequest queryRequest) {
    final QueryResponse queryResponse;
    try {
      log.info(String.format("DYNAMO DB CLIENT - Executing the query: %s", queryRequest));
      queryResponse = dynamoDbClient.query(queryRequest);
    } catch (final Exception e) {
      throw new DbErrorException(e.getMessage());
    }

    log.info("CAMP REQUEST MAP MAPPER - Mapping to entities.");
    return queryResponse
            .items()
            .stream()
            .map(campRequestMapper::toEntity)
            .collect(Collectors.toList());
  }
}
