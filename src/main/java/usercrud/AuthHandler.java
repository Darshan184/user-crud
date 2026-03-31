package usercrud;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayCustomAuthorizerEvent;
import com.amazonaws.services.lambda.runtime.events.IamPolicyResponse;
import java.util.Collections;
import java.util.List;

public class AuthHandler implements RequestHandler<APIGatewayCustomAuthorizerEvent, IamPolicyResponse>{
    @Override
    public IamPolicyResponse handleRequest(APIGatewayCustomAuthorizerEvent event, Context context){
        String token= event.getAuthorizationToken();

        boolean authorized=(token!= null && token.startsWith("Bearer "));
        String effect= authorized? "Allow": "Deny";
        IamPolicyResponse.Statement statement = IamPolicyResponse.Statement.builder()
                .withAction("execute-api:Invoke")
                .withEffect(effect)
                .withResource(Collections.singletonList(event.getMethodArn()))
                .build();
        IamPolicyResponse.PolicyDocument policyDocument = IamPolicyResponse.PolicyDocument.builder()
                .withVersion("2012-10-17")
                .withStatement(Collections.singletonList(statement))
                .build();

        return IamPolicyResponse.builder()
                .withPrincipalId("user")
                .withPolicyDocument(policyDocument)
                .build();

    }
}