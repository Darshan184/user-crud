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
2.To list the users send a POST request to the url --https://192syqoxeh.execute-api.us-east-1.amazonaws.com/Prod/users/list
Give the email id in the form of
```json
[
         "darshan@1234",
         "england@134"
    
    ]
```

3.You can send a DELETE request with url and the body having the userIds that are to be deleted 
```json
[
    
         "darshan@1234",
         "england@134"
    
    
        
]

```
3. To update a record send a PUT request to the url with the function body
```json
{
    "name": "darshan1",
        "email": "darshan@1234"
}
```
Creating multiple users:
<img width="1371" height="835" alt="image" src="https://github.com/user-attachments/assets/f937f4c4-9621-4935-b0a8-d17b8b4be510" />
Updating a user:
<img width="1363" height="832" alt="image" src="https://github.com/user-attachments/assets/7fe82c9d-5314-473d-ac9d-870f8dba47a6" />
Reading a user:
<img width="1361" height="692" alt="image" src="https://github.com/user-attachments/assets/8775dedc-8918-4d4c-9822-d149e52109fe" />
Deleting users:
<img width="1364" height="822" alt="image" src="https://github.com/user-attachments/assets/a5d709d4-bcf2-4709-a2b6-8d7b04732c77" />
Getting multiple users based on the email:
<img width="1370" height="814" alt="image" src="https://github.com/user-attachments/assets/cc2cb534-30c5-40ad-86fd-36009dc31a50" />
Implemented email validation:
<img width="1364" height="801" alt="image" src="https://github.com/user-attachments/assets/4d1471d1-09f2-48ab-b12e-83984f842522" />
<img width="1360" height="834" alt="image" src="https://github.com/user-attachments/assets/44c99205-44f6-4365-99e1-19d3f61b4698" />
With custom lambda authorizer:
<img width="1361" height="801" alt="image" src="https://github.com/user-attachments/assets/0bb873e5-868f-47ec-9116-65a8632b1a20" />
<img width="1366" height="827" alt="image" src="https://github.com/user-attachments/assets/7de1ee3a-a206-4990-ae97-3ed1572732a3" />


