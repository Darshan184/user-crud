This is an assignment implementing Crud operations for a user
URL:https://192syqoxeh.execute-api.us-east-1.amazonaws.com/Prod/users
Steps to use:
1.Send a POST request to the url with example :
```json
[{
    "name":"abcde",
    "email" :"abcde@123.com"
},
{

        
        "name": "darshan1",
        "email": "darshan@123"
    
}
]

```
2.Take the userId from the response and send a GET request with the userId to the url to get the user
3.You can handle getting multiple users by sending a GET request with the body having
3.You can send a DELETE request with url and the body having the userIds that are to be deleted 
```json
[
    
         "9bc9b9a0-b2fb-49af-87e3-a130bad43cb3",
        
         "d0c2d002-2124-4cb7-b4ee-7e39e06958b7"
        
]

```
