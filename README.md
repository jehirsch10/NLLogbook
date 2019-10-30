# NLLogbook
Natural Language Logbook


Project to explore using SpringBoot, Markdown, and light natural language processing
to improve olog-es and thus to improve olog-service.

*The curl command to set up the index properly is included in the mappind_definitions.sh file.*


**Current Project Status:**
. . .

PUT operation to create a logbook works correctly.
It takes an id number in the uri and markdown(commonmark)source code in the body and creates a logbook.
Currently if the id of the logbook to be created is already in use, the operation will replace the old logbook with the new one. 

Example body(using 1 in uri): 
```
{
    "id": 1,
    "src": "This is *Sparta*!",
    "description": "This is Sparta!"
}
```

GET operation to find a logbook by ID works correctly.
It takes an id number in the uri and returns the log entry or a 404 error.

##GET operation to search for a logbook using a more-like-this query is now implemented.
Using the previously mentioned Sparta example, some queries that will find it include:
-is
-This
-fdfsdf is dfd dfdf fdsd
-Sparta*!
-sparta
And some queries that will not find it include:
-tis
-fdf fdsf tis fdsfsd
-i s

GET operation to search for a logbook using fuzzy searching is **NOT** yet implemented.

GET operation to search for a logbook using a more-like-this query and fuzzy searching is **NOT** yet implemented.

*Note that authentication/authorization is not required.
