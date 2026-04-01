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


public class BulkUsersHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private final DynamoDbTable<User> table;
    private final ObjectMapper mapper = new ObjectMapper();//Object mapper to map json objects
    private static final DynamoDbEnhancedClient client = DynamoDbEnhancedClient.create();

    public BulkUsersHandler() {
        String tableName = System.getenv("TABLE_NAME");
        if (tableName == null)//Setting default tableName to UsersTable
            tableName = "UsersTable";
        this.table = client.table(tableName, TableSchema.fromBean(User.class));
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        String method = request.getHttpMethod();
        String body = request.getBody();
        Map<String, String> pathParams = request.getPathParameters();
        try {
            List<String>  emails = getEmailsFromRequest(request);
            if (emails.isEmpty())
                return response(400, "Missing user ids");
            ReadBatch.Builder<User> readBatch = ReadBatch.builder(User.class).mappedTableResource(table);
            emails.forEach(email -> readBatch.addGetItem(Key.builder().partitionValue(email).build()));
            List<User> results = client.batchGetItem(b -> b.addReadBatch(readBatch.build()))
                    .resultsForTable(table).stream().collect(Collectors.toList());
            if (results.isEmpty())
                return response(404, "User not found");
            return response(200, results);
        }catch(Exception e){
            APIGatewayProxyResponseEvent errorRes = new APIGatewayProxyResponseEvent();
            errorRes.setStatusCode(500);
            errorRes.setBody("Server Error: " + e.getMessage());
            return errorRes;
        }
    }
    private List<String> getEmailsFromRequest(APIGatewayProxyRequestEvent request) throws Exception{
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
