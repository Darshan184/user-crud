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
import software.amazon.awssdk.enhanced.dynamodb.model.TransactPutItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchGetItemEnhancedRequest;
import com.fasterxml.jackson.core.type.TypeReference;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchGetResultPageIterable;
import java.util.Set;

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
                            ? mapper.readValue(body, new TypeReference<List<User>>() {})
                            : Collections.singletonList(mapper.readValue(body, User.class));
                    if (usersPosted.isEmpty()) return response(400, "Empty request body");
                    //List of emails which are invalid
                    List<String> invalidEmails = usersPosted.stream()
                            .map(User::getEmail)
                            .filter(email -> !isValidEmail(email))
                            .collect(Collectors.toList());

                    if (!invalidEmails.isEmpty()) {
                        return response(400, "Invalid email format(s) detected: " + invalidEmails);
                    }

                    //Reading the batch of items in the body from the dynamodb to see if it already exists
                    ReadBatch.Builder<User> readBatch = ReadBatch.builder(User.class).mappedTableResource(table);
                    usersPosted.forEach(user -> readBatch.addGetItem(Key.builder().partitionValue(user.getEmail()).build()));

                    // Fetch results and convert to List
                    List<User> existingUsers = client.batchGetItem(b -> b.addReadBatch(readBatch.build()))
                            .resultsForTable(table)
                            .stream()
                            .collect(Collectors.toList());

                    // Set of existing emails
                    Set<String> existingEmails = existingUsers.stream()
                            .map(User::getEmail)
                            .collect(Collectors.toSet());

                    // filter the users to create
                    List<User> usersToCreate = usersPosted.stream()
                            .filter(u -> !existingEmails.contains(u.getEmail()))
                            .collect(Collectors.toList());

                    // 5. Batch Write
                    if (!usersToCreate.isEmpty()) {
                        client.batchWriteItem(b -> {
                            WriteBatch.Builder<User> writeBuilder = WriteBatch.builder(User.class).mappedTableResource(table);
                            usersToCreate.forEach(writeBuilder::addPutItem);
                            b.addWriteBatch(writeBuilder.build());
                        });
                    }

                    return response(201, Map.of(
                            "createdCount", usersToCreate.size(),
                            "existingUsers", existingUsers
                    ));
                }
                case "PUT": {
                    Map<String, Object> authorizer = request.getRequestContext().getAuthorizer();
                    String callerEmail = (authorizer != null) ? (String) authorizer.get("email") : null;

                    if (callerEmail == null) {
                        return response(401, "Unauthorized: No valid token found");
                    }

                    List<User> usersToUpdate = (body != null && body.trim().startsWith("["))
                            ? mapper.readValue(body, new TypeReference<List<User>>() {})
                            : Collections.singletonList(mapper.readValue(body, User.class));
                    if (usersToUpdate.isEmpty()) return response(400, "Empty request body");

                    String firstEmailInBody = usersToUpdate.get(0).getEmail();

                    if (!firstEmailInBody.equalsIgnoreCase(callerEmail)) {
                        return response(403, "Forbidden: You can only initiate updates for your own account.");
                    }

                    User existingUser = table.getItem(Key.builder().partitionValue(firstEmailInBody).build());

                    if (existingUser == null) {
                        return response(404, "Error: The primary user " + firstEmailInBody + " was not found in the database.");
                    }

                    WriteBatch.Builder<User> putBatch = WriteBatch.builder(User.class).mappedTableResource(table);
                    usersToUpdate.forEach(putBatch::addPutItem);
                    client.batchWriteItem(b -> b.addWriteBatch(putBatch.build()));

                    return response(200, Map.of("status", "Update successful", "verifiedUser", firstEmailInBody));
                }

               /*
                case "GET": {
                    List<String> ids = getIdsFromRequest(request);
                    if (ids.isEmpty())
                        return response(400, "Missing user ids");
                    ReadBatch.Builder<User> readBatch = ReadBatch.builder(User.class).mappedTableResource(table);
                    ids.forEach(id -> readBatch.addGetItem(Key.builder().partitionValue(id).build()));
                    List<User> results = client.batchGetItem(b -> b.addReadBatch(readBatch.build()))
                            .resultsForTable(table).stream().collect(Collectors.toList());
                    if (results.isEmpty())
                        return response(404, "User not found");
                    return response(200, results);
                }*/
                case "DELETE": {
                    List<String> emails = getEmailsFromRequest(request);
                    if (emails.isEmpty())
                        return response(400, "Missing UserId");
                    WriteBatch.Builder<User> deleteBatch = WriteBatch.builder(User.class).mappedTableResource(table);
                    emails.forEach(email -> deleteBatch.addDeleteItem(Key.builder().partitionValue(email).build()));
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
    //Using this function to get emails
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
    private boolean isValidEmail(String email) {
        if (email == null) return false;
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        return email.matches(emailRegex);
    }
}
