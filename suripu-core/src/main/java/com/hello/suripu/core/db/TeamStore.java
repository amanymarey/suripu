package com.hello.suripu.core.db;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeAction;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.AttributeValueUpdate;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.amazonaws.services.dynamodbv2.model.DeleteItemRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.QueryRequest;
import com.amazonaws.services.dynamodbv2.model.QueryResult;
import com.amazonaws.services.dynamodbv2.model.UpdateItemRequest;
import com.google.common.base.Optional;
import com.hello.suripu.core.models.Team;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TeamStore {

    private static final Logger LOGGER = LoggerFactory.getLogger(TeamStore.class);

    public enum Type {
        DEVICES("devices"),
        USERS("users");

        private final String value;
        private Type(final String value) {
            this.value = value;
        }
    }

    private final AmazonDynamoDB dynamoDB;
    private final String tableName;


    public TeamStore(final AmazonDynamoDB dynamoDB, final String tableName) {
        this.dynamoDB = dynamoDB;
        this.tableName = tableName;
    }

    public void createTeam(final Team team, final Type type) {

        final Map<String, AttributeValue> item = new HashMap<>();
        item.put("type", new AttributeValue().withS(type.toString()));
        item.put("name", new AttributeValue().withS(team.name));
        item.put("members", new AttributeValue().withSS(team.ids));

        final PutItemRequest putItemRequest = new PutItemRequest();
        putItemRequest.withTableName(tableName)
                .withItem(item);

        dynamoDB.putItem(putItemRequest);
    }


    public Optional<Team> getTeam(final String teamName, final Type type) {

        final Map<String, AttributeValue> item = new HashMap<>();
        item.put("type", new AttributeValue().withS(type.toString()));
        item.put("name", new AttributeValue().withS(teamName));


        final GetItemRequest getItemRequest = new GetItemRequest();
        getItemRequest.withTableName(tableName)
                .withKey(item);

        final GetItemResult result = dynamoDB.getItem(getItemRequest);
        return makeTeam(result.getItem(), type);
    }


    public List<Team> getTeams(final Type type) {

        final Map<String, Condition> conditions = new HashMap<>();
        conditions.put("type", new Condition()
                .withComparisonOperator(ComparisonOperator.EQ)
                .withAttributeValueList(new AttributeValue().withS(type.toString())));

        conditions.put("name", new Condition()
                .withComparisonOperator(ComparisonOperator.GT)
                .withAttributeValueList(new AttributeValue().withS(" ")));

        final QueryRequest queryRequest = new QueryRequest();
        queryRequest.withTableName(tableName)
                .withKeyConditions(conditions)
                .withLimit(100);

        final QueryResult result = dynamoDB.query(queryRequest);

        final List<Team> teams = new ArrayList<>();
        for(Map<String, AttributeValue> attributeValueMap : result.getItems()) {
            Optional<Team> team = makeTeam(attributeValueMap, type);
            if(team.isPresent()) {
                teams.add(team.get());
            }
        }

        return teams;
    }


    public void add(final String teamName, final Type type, final List<String> ids) {
        update(teamName, type, ids, AttributeAction.ADD);
    }

    public void remove(final String teamName, final Type type, final List<String> ids) {
        update(teamName, type, ids, AttributeAction.DELETE);
    }

    public void delete(final Team team, final Type type) {
        final DeleteItemRequest deleteItemRequest = new DeleteItemRequest();
        deleteItemRequest.withTableName(tableName);
        final Map<String, AttributeValue> attributes = new HashMap<>();
        attributes.put("type", new AttributeValue().withS(type.toString()));
        attributes.put("name", new AttributeValue().withS(team.name));
        deleteItemRequest.withKey(attributes);

        dynamoDB.deleteItem(deleteItemRequest);
    }


    /**
     * Converts DynamoDB map to Team object
     * @param attributes
     * @param type
     * @return Optional of Team
     */
    private Optional<Team> makeTeam(Map<String, AttributeValue> attributes, Type type) {
        if(!attributes.containsKey("name") || !attributes.containsKey("members")) {
            return Optional.absent();
        }

        final Set<String> members = new HashSet<String>();
        members.addAll(attributes.get("members").getSS());
        final Team team = new Team(attributes.get("name").getS(), members);
        return Optional.of(team);
    }

    /**
     * Adds or removes members from set
     * @param teamName
     * @param type
     * @param deviceId
     * @param attributeAction
     */
    private void update(final String teamName, final Type type, final List<String> deviceId, final AttributeAction attributeAction) {
        final UpdateItemRequest updateItemRequest = new UpdateItemRequest();
        final Map<String, AttributeValue> keys = new HashMap<>();
        keys.put("type", new AttributeValue().withS(type.toString()));
        keys.put("name", new AttributeValue().withS(teamName));
        final AttributeValueUpdate update = new AttributeValueUpdate()
                .withAction(attributeAction)
                .withValue(new AttributeValue().withSS(deviceId));

        updateItemRequest
                .withTableName(tableName)
                .withKey(keys)
                .addAttributeUpdatesEntry("members", update);

        dynamoDB.updateItem(updateItemRequest);
    }

    public static List<String> longsToStrings(final List<Long> ids) {
        final List<String> newIds = new ArrayList<>();
        for(final Long id : ids) {
            newIds.add(String.valueOf(id));
        }

        return newIds;
    }
}
