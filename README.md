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
Creating multiple users:
<img width="1371" height="835" alt="image" src="https://github.com/user-attachments/assets/f937f4c4-9621-4935-b0a8-d17b8b4be510" />
Updating a user:
<img width="1355" height="829" alt="image" src="https://github.com/user-attachments/assets/1b172f6f-db7d-4bf7-b6f9-aea8016ed783" />
Reading a user:
<img width="1361" height="692" alt="image" src="https://github.com/user-attachments/assets/8775dedc-8918-4d4c-9822-d149e52109fe" />
Deleting users:
<img width="1364" height="822" alt="image" src="https://github.com/user-attachments/assets/a5d709d4-bcf2-4709-a2b6-8d7b04732c77" />



