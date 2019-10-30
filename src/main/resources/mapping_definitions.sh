curl -H 'Content-Type: application/json' -XPUT http://localhost:9200/logfinder -d'
{
"mappings":{
  "logbook" : {
    "properties" : {
        "id" : {
        "type" : "keyword"
      },
"src" : {
        "type" : "keyword"
      },
      "description" : {
        "type" : "text"
      }
    }
  }
  }
}'