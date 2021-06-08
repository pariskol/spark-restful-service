## Description

This library is based on Spak Java web framework and its main goal is to avoid bolierplate code for basic tasks (such as creating cors filter, defining a json serializer, jdbc boilerplate code for crud operations etc.).Create a basic CRUD Restful service with basic authentication and json responses with just a few lines of code !

Supported databases:
- MySQL (use MysqlConnector class)
- MariaDB (use MysqlConnector class)
- SQLite (use SqliteConnector class)

The simplest example to expose all tables of an sqlite database.  

```java
CRUDRESTfulService service = new CRUDRESTfulService();
// for /api/save/:table to execute updates too , read doc
service.enableUpdateMode(true);
service.configure("localhost", 8080);
service.init(new MysqlConnector(dbUrl, dbName, dbUsername, dbPassword));
service.start();

```

A More advanced example with custom routes, limited exposed tables and mysql 

```java

public class MyRESTfulService extends CRUDRESTfulService {

	@Override
	protected void declareFilters() {
        super.declareFilters();
        filter("/api/download/*", (request, response) -> this::authorize);
        filter("/api/upload", (request, response) -> this::authorize);
	}
	
	@Override
	protected void declareEndpoints() {
        // getData (built in) behaves exactly like default api get
        get("/api/get/messages", (request, response) -> getData(request, response, "messages"));
        // saveData (built in) behaves exactly like default api save
        post("/api/save/message", (request, response) -> saveData(request, response, "messages"));
        get("/api/get/files", (request, response) -> getData(request, response, "files"));
        post("/api/doStuff", this::doStuff);
	}

	// MyResponse object will be auto serialized as json
	public MyResponse doStuff(Request request, Response response) throws Exception {
	...
	}


    public static void main(String[] args) {
        MyRESTfulService service = new MyRESTfulService();
        service.enableUpdateMode(true);
        service.configure("localhost", 8080);
        service.init(new MysqlConnector(dbUrl, dbUsername, dbPassword));
        service.start();
    }
}


```

## Default api spec

- GET /api/get/:table

with required basic auth header \
with optional url parameters for filtering, matching table's columns names and types (ex. column age INT -> ?age=5)
- POST /api/save/:table 

with required basic auth header \
with json body with properties matcing table's columns names and types (if primary key is present an update is performed instead of an insert)

example : for the following mysql table

```sql

CREATE TABLE students (
    id INT PRIMARY KEY;
    name VARCHAR(64);
    age INT;
)
```
url will be /api/save/students \
for insert provide following json 

```json
{
    "name" : "Carlos",
    "age": 21
}
```
for update provide following json (you should know entry's primary key value)

```json
{
    "id": 4312,
    "name" : "Carlos",
    "age": 23
}
```


- POST /api/delete/:table 

with required basic auth header \
with json body containing table's primary key (ex: { "id" : 5 })

## Build fat jar
  
mvn clean compile assembly:single

JDBC prefered connector lib jar is not bundled into the fat jar to reduce size for faster deployments. You should included later in the class path. If you want connector jar to be included change scope in pom.xml.

## Common usage

- Create a database
- Create linux service for fat jar , provide scripts/run.sh script
- Create a property file for configuration
- Add nginx configuration to set up a reverse proxy
- Get a lets encrypt cert with certbot
- Run scripts/create-p12-for-jetty.sh to create a p12 file from retreived lets encrypt ssl cert
