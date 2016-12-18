# ACME4J Manager

This app was created to fully automatically create, and renew Let's encrypt certificates and deploy the certificates to the free SNI endpoint on Heroku. However the ACME Manager is written as a modular and configurable application so that it is useful for other (similar) use cases as well.
 
This repository has the following modules:
 
 1. **acme4j-manager**: This is the fully configurable core module which contains the main logic of requesting, signing and storing certificates. The ACME interaction uses the [acme4j](https://github.com/shred/acme4j/) library, storage, TOS display etc are only interface-skeletons that allow the core module to be used in many different scenarios.
 2. **simple-jdbc**: A simple helper library that wraps the JDBC API and makes working with it less painful.
 3. **acme4j-deploy-heroku**: A simple helper library that includes methods to deploy certificates to Heroku.
 4. **acme4j-heroku-client**: An actual usable application that uses the three modules listed above and binds them together so that the application stores certificates in a PostgreSQL-Database and deploys vertificates to heroku automatically. 

Checkout the Code and the [README of acme4j-heroku-client](acme4j-heroku-client/README.md) to see everything in action.
