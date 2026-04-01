package usercrud;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.services.dynamodb.model.*;
import software.amazon.awssdk.enhanced.dynamodb.*;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.Collections;
import java.util.List;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.WriteBatch;
import software.amazon.awssdk.enhanced.dynamodb.model.ReadBatch;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchWriteItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchGetItemEnhancedRequest;
import com.fasterxml.jackson.core.type.TypeReference;


public class UserHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent>{
    private final DynamoDbTable<User> table;
    private final ObjectMapper mapper= new ObjectMapper();//Object mapper to map json objects
    private static final  DynamoDbEnhancedClient client= DynamoDbEnhancedClient.create();

    public UserHandler(){
        String tableName= System.getenv("TABLE_NAME");
        if(tableName==null)//Setting default tableName to UsersTable
            tableName="UsersTable";
        this.table=client.table(tableName,TableSchema.fromBean(User.class));
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context){
        String method= request.getHttpMethod();//The HTTP method is extracted inorder to switch between crud functions
        String body=request.getBody();
        Map<String,String> pathParams= request.getPathParameters();
        try {
            switch (method){
                case "POST": {
                    List<User> usersPosted = (body != null && body.trim().startsWith("["))
                            ? mapper.readValue(body, new TypeReference<List<User>>() {
                    })
                            : Collections.singletonList(mapper.readValue(body, User.class));
                    if (usersPosted.isEmpty())
                        return response(400, "Empty request body");
                    WriteBatch.Builder<User> writeBatch = WriteBatch.builder(User.class).mappedTableResource(table);
                    for (User user : usersPosted) {
                        if (user.getUserId() == null)
                            user.setUserId(UUID.randomUUID().toString());
                        writeBatch.addPutItem(user);
                    }
                    client.batchWriteItem(b -> b.addWriteBatch(writeBatch.build()));
                    return response(201, usersPosted);
                }
                case "GET": {
                    List<String> ids = getIdsFromRequest(request);
                    if (ids.isEmpty())
                        return response(400, "Missing user ids");
                    ReadBatch.Builder<User> readBatch = ReadBatch.builder(User.class).mappedTableResource(table);
                    ids.forEach(id -> readBatch.addGetItem(Key.builder().partitionValue(id).build()));
                    List<User> results = client.batchGetItem(b -> b.addReadBatch(readBatch.build()))
                            .resultsForTable(table).stream().collect(Collectors.toList());
                    if (ids.size() == 1 && results.isEmpty())
                        return response(404, "User not found");
                    return response(200, results);
                }
                case "DELETE": {
                    List<String> ids = getIdsFromRequest(request);
                    if (ids.isEmpty())
                        return response(400, "Missing UserId");
                    WriteBatch.Builder<User> deleteBatch = WriteBatch.builder(User.class).mappedTableResource(table);
                    ids.forEach(id -> deleteBatch.addDeleteItem(Key.builder().partitionValue(id).build()));
                    client.batchWriteItem(b -> b.addWriteBatch(deleteBatch.build()));
                    return response(204, null);
                }
                default:
                    return response(405,"Unsupported Method");
            }
        }catch(Exception e){
            //Here we handle the exception which can be thrown by mapper.writeValueAsString
            APIGatewayProxyResponseEvent errorRes = new APIGatewayProxyResponseEvent();
            errorRes.setStatusCode(500);
            errorRes.setBody("Server Error: " + e.getMessage());
            return errorRes;
        }
    }
    //Using this function to get ids
    private List<String> getIdsFromRequest(APIGatewayProxyRequestEvent request) throws Exception{
        Map<String,String> pathParams= request.getPathParameters();
        if(pathParams !=null && pathParams.containsKey("id"))
            return Collections.singletonList(pathParams.get("id"));
        String body=request.getBody();
        if(body!=null && !body.trim().isEmpty())
            return mapper.readValue(body, new TypeReference<List<String>>() {});
        return Collections.emptyList();
    }

    private APIGatewayProxyResponseEvent response(int status, Object body) {
        try {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(status)
                    .withBody(body != null ? mapper.writeValueAsString(body) : "");
        }catch(Exception e){
            return new APIGatewayProxyResponseEvent().withStatusCode(500).withBody("Internal Server Error");
        }
    }
}
