This is an assignment implementing Crud operations for a user
URL:https://192syqoxeh.execute-api.us-east-1.amazonaws.com/Prod/users
Steps to use:
1.Send a POST request to the url with example :
```json
{
    "name":"abcde",
    "email" :"abcde@123.com"
}

```
2.Take the userId from the response and send a GET request with the userId to the url to get the user
3.You can send a DELETE request with url/{userId} to delete the user
