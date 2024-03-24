from fastapi import FastAPI, HTTPException, status
from pydantic import BaseModel, EmailStr
from passlib.context import CryptContext
from fastapi.middleware.cors import CORSMiddleware
import mysql.connector
from typing import List
from datetime import date
import jwt
import os
from fastapi.security import OAuth2PasswordBearer
from fastapi import BackgroundTasks
from fastapi_mail import FastMail, MessageSchema, ConnectionConfig


app = FastAPI()

# Configure CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Database and Mysql Connection

MYSQL_HOST = "127.0.0.1"
MYSQL_USER = "root"
MYSQL_PASSWORD = "root"
MYSQL_DATABASE = "todo_list"

def create_database():
    try:
        # Establish a connection without specifying a database
        conn = mysql.connector.connect(
            host=MYSQL_HOST,
            user=MYSQL_USER,
            password=MYSQL_PASSWORD,
            port=3306
        )

        # Create a cursor object to execute SQL queries
        cursor = conn.cursor()

        # Create the 'todo_list' database if it doesn't exist
        create_database_query = f"CREATE DATABASE IF NOT EXISTS {MYSQL_DATABASE}"
        cursor.execute(create_database_query)

        # Use the 'todo_list' database
        cursor.execute(f"USE {MYSQL_DATABASE}")

        # Commit the changes to the database
        conn.commit()
        
        # # Create the 'todo_users' table if it doesn't exist
        create_users_table_query = """
        CREATE TABLE IF NOT EXISTS todo_users (
            id INT AUTO_INCREMENT PRIMARY KEY,
            firstname VARCHAR(255) NOT NULL,
            lastname VARCHAR(255) NOT NULL,
            email VARCHAR(255) NOT NULL UNIQUE,
            phone VARCHAR(25) NOT NULL,
            password VARCHAR(255) NOT NULL
        )
        """

        # Create the 'todo_tasks' table if it doesn't exist
        create_tasks_table_query = """
        CREATE TABLE IF NOT EXISTS todo_tasks (
            id INT AUTO_INCREMENT PRIMARY KEY,
            title VARCHAR(255) NOT NULL,
            description TEXT,
            start_date varchar(20) NOT NULL,
            end_date varchar(20) NOT NULL,
            priority VARCHAR(50) NOT NULL,
            owner_id INT NOT NULL,
            status INT DEFAULT 1,
            GroupId varchar(50) not null,
            FOREIGN KEY (owner_id) REFERENCES todo_users(id)
        )
        """

        # Create the 'task_user_relation' table if it doesn't exist
        create_relation_table_query = """
        CREATE TABLE IF NOT EXISTS task_user_relation (
            id INT AUTO_INCREMENT PRIMARY KEY,
            task_id INT NOT NULL,
            user_id INT NOT NULL,
            FOREIGN KEY (task_id) REFERENCES todo_tasks(id),
            FOREIGN KEY (user_id) REFERENCES todo_users(id)
        )
        """
        cursor.execute(create_users_table_query)
        cursor.execute(create_tasks_table_query)
        cursor.execute(create_relation_table_query)

        # Close the connection
        cursor.close()
        conn.close()

    except Exception as e:
        print(f"An error occurred while creating the database: {str(e)}")

# Call the function to create the database
create_database()

# Establish connection to MySQL server
db_conn = mysql.connector.connect(
    host=MYSQL_HOST,
    user=MYSQL_USER,
    password=MYSQL_PASSWORD,
    database=MYSQL_DATABASE,
    port=3306
)

# Create a cursor object to execute SQL queries
cursor = db_conn.cursor(dictionary=True)


SECRET_KEY = "mysecretkey"
ALGORITHM = "HS256"
PASSWORD_HASHING_ALGORITHM = "bcrypt"

pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")

# Email configuration
class Envs:
    MAIL_USERNAME = 'infotechg87@gmail.com'
    MAIL_PASSWORD = 'ouct ffmr lyoj kmaf'
    MAIL_FROM = 'infotechg87@gmail.com'
    MAIL_PORT = '587'
    MAIL_SERVER = 'smtp.gmail.com'
    MAIL_FROM_NAME = 'Password Reset'

template_folder_path = os.path.abspath('./templates')

conf = ConnectionConfig(
    MAIL_USERNAME=Envs.MAIL_USERNAME,
    MAIL_PASSWORD=Envs.MAIL_PASSWORD,
    MAIL_FROM=Envs.MAIL_FROM,
    MAIL_PORT=Envs.MAIL_PORT,
    MAIL_SERVER=Envs.MAIL_SERVER,
    MAIL_FROM_NAME=Envs.MAIL_FROM_NAME,
    MAIL_STARTTLS=True,
    MAIL_SSL_TLS=False,
    USE_CREDENTIALS=True,
    TEMPLATE_FOLDER=template_folder_path
)

class User(BaseModel):
    firstname: str
    lastname: str
    email: EmailStr
    phone: str
    password: str

class UserDTO(BaseModel):
    email: EmailStr
    password: str

class TodoTask(BaseModel):
    title: str
    description: str
    start_date: str
    end_date: str
    priority: str
    user_ids: List[int]
    owner_id: int
    status: int
    GroupId: str
    
    

class TodoTaskUpdate(BaseModel):
    description: str
    user_ids: List[int]

class UpdateUser(BaseModel):
    firstname: str
    lastname: str
    password: str

# Password reset request model
# class PasswordReset(BaseModel):
#     phone: str

# New Password model
class NewPassword(BaseModel):
    # token: str
    phone: str
    password: str

# Function to create a JWT token
def create_jwt_token(data: dict):
    to_encode = data.copy()
    token = jwt.encode(to_encode, SECRET_KEY, algorithm=ALGORITHM)
    return token

# Function to decode and verify a JWT token
def decode_jwt_token(token: str):
    payload = jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM])
    return payload


def create_user(user: User):

    # Check for blank fields
    if not user.firstname or not user.lastname or not user.email or not user.phone or not user.password :
        return {"message": "All fields are required"}

    # elif not user.phone.isdigit() or len(user.phone) != 10:

    #     return {"message": "Invalid phone number"}
    else :

        try :
            # Check for duplicate email addresses
            select_query = "SELECT * FROM todo_users WHERE email = %(email)s"
            cursor.execute(select_query, {"email": user.email})
            existing_user = cursor.fetchone()
            if existing_user:
                return {"message": "Email already registered"}

            # Check for duplicate email addresses
            select_query = "SELECT * FROM todo_users WHERE phone = %(phone)s"
            cursor.execute(select_query, {"phone": user.phone})
            existing_user = cursor.fetchone()
            if existing_user:
                return {"message": "Phone No already registered"}
            
            if len(user.password) < 8:
                return {"message": "Password must be at least 8 characters long"}

            hashed_password = pwd_context.hash(user.password)

            # base64 library use 
            # hashed_password = base64.b64encode(user.password.encode()).decode()

            insert_query = "INSERT INTO todo_users (firstname, lastname, email, phone, password) VALUES (%(firstname)s, %(lastname)s, %(email)s, %(phone)s, %(password)s)"
            cursor.execute(insert_query, {"firstname": user.firstname, "lastname": user.lastname, "email": user.email, "phone": user.phone, "password": hashed_password})
            db_conn.commit()

            return {"message": "User registered successfully"}
        except Exception as e:
            # Handle the exception and return an appropriate error message
            return {"message": f"Failed to User registration. Error: {str(e)}"}

@app.post("/signup", response_model=dict)
async def sign_up(user: User):
    result = create_user(user)
    return result

@app.post("/signin", response_model=dict)
async def sign_in(login: UserDTO):

    email = login.email
    password = login.password

    select_query = "SELECT * FROM todo_users WHERE email = %(email)s"
    cursor.execute(select_query, {"email": email})
    user_data = cursor.fetchone()

    if not user_data:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid credentials")

    stored_password = user_data['password']
    # Verify the password using the correct hashing algorithm
    if not pwd_context.verify(password, stored_password):
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid credentials")

    select_query = "SELECT id,firstname FROM todo_users WHERE email = %(email)s"
    cursor.execute(select_query, {"email": email})
    user_id = cursor.fetchone()
    # print(yser)
    return {"message": "User Login Successfully","uid":user_id['id'],"username":user_id["firstname"]}

@app.get("/get_user/{user_id}", response_model=dict)
async def get_user(user_id: int):
    select_query = "SELECT firstname, lastname,email,phone FROM todo_users WHERE id = %s"
    cursor.execute(select_query, (user_id,))
    user_data = cursor.fetchone()

    if not user_data:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="User not found")

    return user_data

# @app.put("/update_user/{user_id}", response_model=dict)
# async def update_user(user_id: int, updated_user: User):
#     try:
#         # Check if the user exists
#         select_user_query = "SELECT * FROM todo_users WHERE id = %s"
#         cursor.execute(select_user_query, (user_id,))
#         existing_user = cursor.fetchone()

#         if not existing_user:
#             return {"message": "User not found"}

#         # Update the user information
#         update_user_query = """
#             UPDATE todo_users
#             SET firstname = %s, lastname = %s
#             WHERE id = %s
#         """
#         cursor.execute(update_user_query, (updated_user.firstname, updated_user.lastname, user_id))
#         db_conn.commit()

#         return {"message": "User updated successfully"}

#     except Exception as e:
#         # Handle the exception and return an appropriate error message
#         return {"message": f"Failed to update user. Error: {str(e)}"}
    
@app.put("/update_profile", response_model=dict)
async def update_profile(phone: str, updated_data: UpdateUser):
    try:
         # Check if the phone number exists in the database
        select_query = "SELECT * FROM todo_users WHERE phone = %(phone)s"
        cursor.execute(select_query, {"phone": phone})
        existing_user = cursor.fetchone()

        if not existing_user:
            raise HTTPException(status_code=404, detail="User not found")

        # Update the user's profile information
        update_profile_query = """
            UPDATE todo_users
            SET firstname = %(firstname)s, lastname = %(lastname)s, password = %(password)s
            WHERE phone = %(phone)s
        """
        
        if len(updated_data.password) < 8:
                return {"message": "Password must be at least 8 characters long"}
        
        hashed_password = pwd_context.hash(updated_data.password)
        cursor.execute(update_profile_query, {
            "firstname": updated_data.firstname,
            "lastname": updated_data.lastname,
            "password": hashed_password,
            "phone": phone
        })
        db_conn.commit()

        return {"message": "Profile updated successfully"}

    except Exception as e:
        # Handle the exception and return an appropriate error message
        return {"message": f"Failed to update profile. Error: {str(e)}"}    
    
@app.post("/add_task", response_model=dict)
async def add_task(task_data: TodoTask):
    
    try:
        # Insert the task into the todo_tasks table
        insert_task_query = """
            INSERT INTO todo_tasks (title, description, start_date, end_date, priority, owner_id, GroupId,status)
            VALUES (%s, %s, %s, %s, %s, %s,%s,1)
        """
        print( task_data.title,
            task_data.description,
            task_data.start_date,
            task_data.end_date,
            task_data.priority,
            task_data.owner_id,
            task_data.GroupId,
           )
        cursor.execute(insert_task_query, (
            task_data.title,
            task_data.description,
            task_data.start_date,
            task_data.end_date,
            task_data.priority,
            task_data.owner_id,
            task_data.GroupId,
            # task_data.status
        ))
        db_conn.commit()

        # Retrieve the ID of the inserted task
        task_id = cursor.lastrowid

        # Insert task-user relations into the task_user_relation table
        insert_relation_query = """
            INSERT INTO task_user_relation (task_id, user_id)
            VALUES (%s, %s)
        """
        for user_id in task_data.user_ids:
            cursor.execute(insert_relation_query, (task_id, user_id))
            db_conn.commit()

        return {"message": "Task added successfully"}

    except Exception as e:
        # Handle the exception and return an appropriate error message
        return {"message": f"Failed to add task. Error: {str(e)}"}
    
@app.put("/update_task/{task_id}", response_model=dict)
async def update_task(task_id: int,updated_data: TodoTaskUpdate,owner_id):
    
    # Check if the task exists
    select_task_query = "SELECT * FROM todo_tasks WHERE id = %s AND owner_id = %s"
    cursor.execute(select_task_query, (task_id, owner_id))
    existing_task = cursor.fetchone()

    if not existing_task:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Task not found")

    # Update the task description
    update_task_query = """
        UPDATE todo_tasks
        SET description = %s
        WHERE id = %s
    """
    cursor.execute(update_task_query, (updated_data.description, task_id))
    db_conn.commit()

    # Update the assigned users
    # First, delete existing relations
    delete_relations_query = "DELETE FROM task_user_relation WHERE task_id = %s"
    cursor.execute(delete_relations_query, (task_id,))
    db_conn.commit()

    # Then, insert new relations
    insert_relations_query = """
        INSERT INTO task_user_relation (task_id, user_id)
        VALUES (%s, %s)
    """
    for user_id in updated_data.user_ids:
        cursor.execute(insert_relations_query, (task_id, user_id))
        db_conn.commit()

    return {"message": "Task updated successfully"}

@app.get("/users", response_model=List[dict])
async def get_all_users(current_user_id):

    select_query = "SELECT * FROM todo_users WHERE id != %s"
    cursor.execute(select_query, (current_user_id,))
    users_data = cursor.fetchall()

    if not users_data:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail='No other users found')

    return users_data

@app.get("/get_running_tasks", response_model=dict)
async def get_running_tasks(current_user_id):
    # Define a query to retrieve tasks for the current user (either as owner or assigned user)
    select_query = """
    SELECT t.id AS task_id,t.GroupId, t.title, t.description, t.start_date, t.end_date,
           t.priority, t.owner_id, GROUP_CONCAT(tur.user_id) AS user_ids
    FROM (
        SELECT DISTINCT id, title, description, start_date, end_date, priority, owner_id,GroupId
        FROM todo_tasks
        WHERE status = 1
    ) t
    LEFT JOIN task_user_relation tur ON t.id = tur.task_id
    WHERE t.owner_id = %s OR tur.user_id = %s
    GROUP BY t.id
"""

    # Execute the query
    cursor.execute(select_query, (current_user_id,current_user_id))
    user_tasks = cursor.fetchall()

    # Format the result
    formatted_tasks = []
    for task in user_tasks:
        formatted_task = {
            "task_id": task["task_id"],
            "title": task["title"],
            "description": task["description"],
            "start_date": task["start_date"],
            "end_date": task["end_date"],
            "priority": task["priority"],
            "owner_id": task["owner_id"],
            "t.GroupId": task["GroupId"],
            # "owner_firstname": task["owner_firstname"],
            # "owner_lastname": task["owner_lastname"],
            # "user_id": task["user_id"],
            # "firstname": task["firstname"],
            # "lastname": task["lastname"],
        }
        formatted_tasks.append(formatted_task)

    return {"user_tasks": formatted_tasks}

@app.get("/get_complete_tasks", response_model=dict)
async def get_complete_tasks(current_user_id):
    
    # Define a query to retrieve tasks for the current user (either as owner or assigned user)
    select_query = """
    SELECT t.id AS task_id, t.title, t.description, t.start_date, t.end_date,
           t.priority, t.owner_id, GROUP_CONCAT(tur.user_id) AS user_ids
    FROM (
        SELECT DISTINCT id, title, description, start_date, end_date, priority, owner_id
        FROM todo_tasks
        WHERE status = 0
    ) t
    LEFT JOIN task_user_relation tur ON t.id = tur.task_id
    WHERE t.owner_id = %s OR tur.user_id = %s
    GROUP BY t.id
"""

    # Execute the query
    cursor.execute(select_query, (current_user_id,current_user_id))
    user_tasks = cursor.fetchall()

    # Format the result
    formatted_tasks = []
    for task in user_tasks:
        formatted_task = {
            "task_id": task["task_id"],
            "title": task["title"],
            "description": task["description"],
            "start_date": task["start_date"],
            "end_date": task["end_date"],
            "priority": task["priority"],
            "owner_id": task["owner_id"],
            # "owner_firstname": task["owner_firstname"],
            # "owner_lastname": task["owner_lastname"],
            # "user_id": task["user_id"],
            # "firstname": task["firstname"],
            # "lastname": task["lastname"],
        }
        formatted_tasks.append(formatted_task)

    return {"user_tasks": formatted_tasks}

@app.get("/getTaskById/{task_id}", response_model=dict)
async def get_task_by_id(task_id):
    
    # Define the query to retrieve task details by ID
    select_query = """
    SELECT t.id AS task_id, t.title, t.description, t.start_date, t.end_date,
           t.priority, t.owner_id, tu.user_id, u.firstname AS user_firstname,
           u.lastname AS user_lastname, ou.firstname AS owner_firstname,
           ou.lastname AS owner_lastname,GroupId
    FROM todo_tasks t
    LEFT JOIN task_user_relation tu ON t.id = tu.task_id
    LEFT JOIN todo_users u ON tu.user_id = u.id
    LEFT JOIN todo_users ou ON t.owner_id = ou.id
    WHERE t.id = %s
    """

    # Execute the query
    cursor.execute(select_query, (task_id,))
    task_details = cursor.fetchall()
    print(task_details)
    # Check if the task exists
    if not task_details:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Task not found")

    # Format the result
    formatted_task = {
        "task_id": task_details[0]["task_id"],
        "title": task_details[0]["title"],
        "description": task_details[0]["description"],
        "start_date": task_details[0]["start_date"],
        "end_date": task_details[0]["end_date"],
        "priority": task_details[0]["priority"],
        "owner_id": task_details[0]["owner_id"],
        "assigned_users": [
            {
                "user_id": user["user_id"],
                "firstname": user["user_firstname"],
                "lastname": user["user_lastname"],
            }
            for user in task_details
            if user["user_id"] is not None
        ],
        "owner_firstname": task_details[0]["owner_firstname"],
        "owner_lastname": task_details[0]["owner_lastname"],
        "GroupId":task_details[0]["GroupId"]
        
    }

    return  formatted_task

@app.put("/complete_task/{task_id}", response_model=dict)
async def complete_task(task_id: int,owner_id):
    
    try:
        # Check if the task exists
        select_task_query = "SELECT * FROM todo_tasks WHERE id = %s AND owner_id = %s"
        cursor.execute(select_task_query, (task_id, owner_id))
        existing_task = cursor.fetchone()

        if not existing_task:
            raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Task not found")

        # Update the task status to 0 (completed)
        update_status_query = """
            UPDATE todo_tasks
            SET status = 0
            WHERE id = %s
        """
        cursor.execute(update_status_query, (task_id,))
        db_conn.commit()

        return {"message": "Task completed successfully"}

    except Exception as e:
        # Handle the exception and return an appropriate error message
        return {"message": f"Failed to complete task. Error: {str(e)}"}
    
# Forgot Password Api

# Send email function
async def send_email_async(subject: str, email_to: str, body: dict):
    message = MessageSchema(
        subject=subject,
        recipients=[email_to],
        body=body,
        subtype='html',
    )
    
    fm = FastMail(conf)
    await fm.send_message(message, template_name='email.html')
    
# Background task to send email
def send_email_background(background_tasks: BackgroundTasks, subject: str, email_to: str, body: dict):
    message_body = f"{body['title']}\n\nname: {body['name']}\nReset Link: {body['reset_link']}"

    message = MessageSchema(
        subject=subject,
        recipients=[email_to],
        body=message_body,
        subtype='plain',  # Change subtype to 'plain' for a simple text email
    )

    fm = FastMail(conf)
    background_tasks.add_task(fm.send_message, message, template_name=None)
    
# Password reset endpoint
@app.post("/password", response_description="Reset password")
async def reset_request(phone: str, background_tasks: BackgroundTasks):
    try:
        # Check if the email exists in the database
        select_query = "SELECT * FROM todo_users WHERE phone = %(phone)s"
        cursor.execute(select_query, {"phone": phone})
        existing_user = cursor.fetchone()

        if not existing_user:
            return {"message": "Phone No not found"}

    #     # Retrieve the first name associated with the email
    #     first_name = existing_user["firstname"]

    #     # Continue with the password reset logic
    #     token_data = {"sub": user_email.email}
    #     token = create_jwt_token(token_data)
    #     print("token",token)
        
    #     reset_link = f"http://localhost:8000/?token={token}"

    #     background_tasks.add_task(send_email_background, background_tasks, "Password Reset", user_email.email, {
    #     "title": "Password Reset",
    #     "name": first_name,  # Use the retrieved first name
    #     "reset_link": reset_link
    # })
        # print("send Email ")
        return {"message": "Phone No found"}

    except Exception as e:
        print(f"An error occurred: {str(e)}")
        raise HTTPException(status_code=500, detail="Internal Server Error")

def decode_reset_token(token: str):
    try:
        payload = jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM])
        return payload.get('sub')
    except :
        return None

@app.put("/reset_password", response_model=dict)
async def reset_password(reset_data: NewPassword):
    try:
        # Decode and verify the token
        # token_payload = decode_reset_token(reset_data.token)
        # print("token_payload",token_payload)
        # if token_payload is None:
        #     raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid token")

        # Check if the token is still valid
        # expiration_time = datetime.utcfromtimestamp(token_payload["exp"])
        # if expiration_time < datetime.utcnow():
        #     raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Token has expired")
        
        # Check if the phone no exists in the database
        select_query = "SELECT * FROM todo_users WHERE phone = %(phone)s"
        cursor.execute(select_query, {"phone": reset_data.phone})
        existing_user = cursor.fetchone()

        if not existing_user:
            return {"message": "Phone No not found"}
        
        if len(reset_data.password) < 8:
                return {"message": "Password must be at least 8 characters long"}

        # Update the user's password in the database using the email from the token
        update_password_query = """
            UPDATE todo_users
            SET password = %s
            WHERE phone = %s
        """
        hashed_password = pwd_context.hash(reset_data.password)
        print("hashed_password",hashed_password)
        # cursor.execute(update_password_query, (hashed_password, token_payload))
        cursor.execute(update_password_query, (hashed_password,reset_data.phone))
        db_conn.commit()

        return {"message": "Password reset successfully"}

    except Exception as e:
        # Handle the exception and return an appropriate error message
        return {"message": f"Failed to reset password. Error: {str(e)}"}