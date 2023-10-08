# About
a Ktor sever, used with Compose Multiplatform project

https://github.com/andy42/GameToolsMultiplatform


## Setup

### Install database

download and install PostgreSQL
https://www.postgresql.org/

run pgadmin, create database "game_tool"
on first run of server all tables will be created

i am using HeidiSQL to manage the dataBase

### create keystore for ssl
create a keystore.jks in project root
use java sdk keytool to generate a keystore
https://docs.oracle.com/javase/8/docs/technotes/tools/unix/keytool.html

## project config
the file application.conf is used to configure server settings

### environment variables

ADMIN_EMAIL={default-admin-email};
ADMIN_PASSWORD={default-admin-password};
ADMIN_USERNAME={default-admin-username};
DB_PASSWORD={database-Password};
DB_USER=postgres;
KEY_ALIAS={keystore-alias};
KEY_STORE_PASSWORD={keystore-password};
SECRET={jwt-SECRET}

### first run
on first run a new admin will be created with credentials from environment variables. 

Admin userName, password and email environment variables are only used for first run to create the first admin account, they can be blank after

