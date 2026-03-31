package usercrud;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.enhanced.dynamodb.*;
import java.util.Map;
import java.util.UUID;

public class UserHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent>{
    private final DynamoDbTable<User> table;
    private final ObjectMapper mapper= new ObjectMapper();//Object mapper to map json objects

    public UserHandler(){
        DynamoDbEnhancedClient client= DynamoDbEnhancedClient.builder().build();
        String tableName= System.getenv("TABLE_NAME");
        if(tableName==null)//Setting default tableName to UsersTable
            tableName="UsersTable";
        this.table=client.table(tableName,TableSchema.fromBean(User.class));
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context){
        String method= request.getHttpMethod();//The HTTP method is extracted inorder to switch between crud functions
        try {
            switch (method){
                case "POST":
                    User user= mapper.readValue(request.getBody(), User.class);
                    if(user.getName()==null || user.getEmail()==null)
                        return response(400,"Missing required fields: name or email");
                    if(emailExists(user.getEmail())){
                        return response(400,"User already exists");
                    }
                    user.setUserId(UUID.randomUUID().toString());//Generates a random uuid for userId and converts it to string
                    table.putItem(user);
                    return response(201,user);
                case "PUT":
                    String updateId=request.getPathParameters().get("id");
                    User existing = table.getItem(Key.builder().partitionValue(updateId).build());
                    if(existing==null)
                        return response(404,"User not found");
                    User updates= mapper.readValue(request.getBody(), User.class);
                    if (updates.getName() != null) existing.setName(updates.getName());
                    if (updates.getEmail() != null) existing.setEmail(updates.getEmail());
                    table.putItem(existing);
                    return response(200, existing);
                case "GET":
                    String id= request.getPathParameters().get("id");
                    User found= table.getItem(Key.builder().partitionValue(id).build());
                    return (found!=null) ? response(200,found) : response(404,"User not found");
                case "DELETE":
                    String delId=request.getPathParameters().get("id");
                    table.deleteItem(Key.builder().partitionValue(delId).build());
                    return response(204,null);
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
    //Using this function to check if the email exists
    private boolean emailExists(String email){
        //Building a filter expression to filter the scan
        Expression filter= Expression.builder()
                .expression("#e= :emailVal")
                .putExpressionName("#e","email")
                .putExpressionValue(":emailVal",AttributeValue.builder().s(email).build())
                .build();
        return table.scan(ScanEnhancedRequest.builder().filterExpression(filter).build())
                .items().stream().findAny().isPresent();//We use findAny to streamline the process faster
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
