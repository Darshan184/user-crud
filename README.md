This is an assignment implementing Crud operations for a user
URL:https://192syqoxeh.execute-api.us-east-1.amazonaws.com/Prod/users
Steps to use:
1.Send a POST request to the url with example :
```json
[{
    "name":"abcde",
    "email" :"abcde123@gmail.com"
},
{

        
        "name": "darshan1",
        "email": "darshan123@gmail.com"
    
}
]

```
2.To list the users send a POST request to the url --https://192syqoxeh.execute-api.us-east-1.amazonaws.com/Prod/users/list
Give the email id in the form of
```json
[
         "abcde123@gmail.com",
         "darshan123@gmail.com"
    
    ]
```

3.You can send a DELETE request with url and the body having the userIds that are to be deleted 
```json
[
    
         "darshan123@gmail.com",
         "abcde123@gmail.com"
    
    
        
]

```
3. To update a record send a PUT request to the url with the function body
```json
{
    "name": "Darshan1",
        "email": "darshanvs1206@gmail.com"
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
<img width="1307" height="761" alt="image" src="https://github.com/user-attachments/assets/b7f29acc-7038-4407-9209-4c3767305424" />



