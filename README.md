# Getting started


## How it works
The application was built with:

  - [Kotlin](https://github.com/JetBrains/kotlin) as programming language
  - [Javalin](https://github.com/tipsy/javalin) as web framework
  - [Koin](https://github.com/InsertKoinIO/koin) as dependency injection framework
  - [Jackson](https://github.com/FasterXML/jackson-module-kotlin) as data bind serialization/deserialization
    
Infrastructure:
 - [Kafka](https://github.com/apache/kafka) as general event-source broker 
 - [Elasticsearch](https://github.com/elastic/elasticsearch) as a general data-store    
 - [Docker-Compose](https://github.com/docker/compose) for defining and running multi-containers
     
Tests:
  - [junit](https://github.com/junit-team/junit4)
  - [Unirest](https://github.com/Kong/unirest-java) to call endpoints in test

The server is configured to start on [7000](http://localhost:7000/api) with `api` context.

Build:
> ./gradlew clean build

Start the server:
> ./gradlew run

# API Spec


## models:

### List of share records

```JSON
{
  "shared_records": [
    {
      "user_id": "reginaldo",
      "element": "Assumptions!A1",
      "email": "reginaldo3@gmx.ch"
    }
  ]
}
```

### List of share multiple records

```JSON
{
  "shared_records": [
    {
      "user_id": "reginaldo",
      "element": "Assumptions!A1",
      "email": "reginaldolsoares3@pm.me"
    },
    {
      "user_id": "reginaldo",
      "element": "Assumptions!A1",
      "email": "reginaldo3@gmx.ch"
    }
  ]
}
```

## endpoints:




### Create spreadsheet share request

`POST /api/sheet`

Example request body:

```JSON
{
  "commands": [
    {
      "user_id": "reginaldo",
      "sections": [
        {
          "element": "Assumptions!A1",
          "emails": [
            "reginaldolsoares3@pm.me",
            "reginaldo3@gmx.ch"
          ]
        }
      ]
    }
  ]
}
```

Required fields: `user_id`, `element`, `emails`

> curl --request POST \
    --url http://localhost:7000/api/sheet/ \
    --header 'content-type: application/json' \
    --data '{
    "commands": [
      {
        "user_id": "reginaldo",
         "sections": [{
  				"element":  "Assumptions!A1",
        	"emails": ["reginaldolsoares3@pm.me", "reginaldo3@gmx.ch"]						 
  			 }]
      }
    ]
  }'

### Get all spreadsheet shared requests

`GET /api/sheet`

returns a [List of Shared Records](#list-of-shared-records)

> curl --request GET \
    --url http://localhost:7000/api/sheet

### Get spreadsheet shared requests by email

`GET /api/articles/:email`

returns a [List of Shared Records](#list-of-shared-records)

> curl --request GET \
    --url http://localhost:7000/api/sheet/reginaldo3@gmx.ch


#### Answers

- If you had to generate a new document every time a sharing is added/modified, where would you put that piece of code? What changes would have to be done to your project?
 >Subscribing to the share creation event, I would include a fine tuning regarding the poll period as an aggregation (same share to different users) to leverage a bulk creation.
 I've created a sample subscription, look for subscriber.SpreadsheetCreationConsumer 

- How was your API design process and what was your motivation on the choice of patterns?
 >I've chosen a minimalist http framework also a basic domain based design, through the event-based (kafka) structure it's to extend and scale this application. 
