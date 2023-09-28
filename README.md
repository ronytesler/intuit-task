The 'home-task' project is the REST api service. It accesses the db and serve the clients.

The 'csv-monitor' project monitors the players.csv file, and after the file is saved, it checks for changes and send a
message, using RabbitMQ, with the new/modified player, to the 'home-task' service.
The flow for checking if a player has been modified or is new -
When the service starts, I check if a db_state.json file exists.
The db_state.json file contains for each player a mappings between the playerID and the hash of the player's line in the
file.
If it exists, I check if its last modified time is before the last modified time of the players.csv file.
If it doesn't exist, or if its last modified time is earlier, I read the players.csv file line by line.
For each line, I compare the hash (SHA-256) of the line, with the hash in the db_state.json file for the same playerId (
if exists). If they're not equal, I add/update the hash to/in the file.
If it does exist and its last modified time is newer, I read the file and keep it as a Map in memory.
I use a WatchService in order to be notified when the player.csv is modified. When being notified, I again scan the file
and compare the hashes and send Queue Messages etc.
assumptions:

* As long as we deal with a file of baseball players, we can assume it will contain a reasonable amount of rows.
* Almost half of the players have died, and a lot of them have retired, so it make sense to not expect large amount of
  updates (also by looking at the fields' nature, although new ones can be added).
* We might want to search or filter by specific columns, so the data is stored as a document with each column in the
  file corresponding to a field in the object, where the playerID is the _id field in MongoDB.
* Because the task was to serve the file content, there might be a need to serve the content even if it changes. I
  assume the file is where the content is handled, and there's no other interface for managing the players' data.
* I assume the data in the file can be handled as it is, and there's no need for modifications and verification (for
  example, a future/negative birth year or a month/day not in range).
* Because the data of each player should not change a lot, it makes sense to put it in a cache, but because I'm not sure
  if the db is going to be changed by other sources, I add a ttl to it.
* No authentication is needed - everyone can access the api.
* No proxy server or load balancer is used. No throttling, rate limiting or DDoS protection.

not covered:

* Synchronizing the db to the memory state and the csv file -
  Although I assumed the Excel file is the interface for managing the data, I don't know if other systems/people are
  going to alter the database content.
  I could add an "updated_time" to each document that is automatically updated when an update to the document happens (
  or when the document is inserted), then when the system loads - sync with the documents that were updated since our
  last sync (last sync time is also saved in the db, for using the same clock).

* load testing -
  Didn't test how many requests per seconds can be performed, with different cache misses/hits amount.

* security -
  Didn't create a user for rabbitmq with non default values for user and password

* files synchronization edge cases -
  An update fails, then another one for the same player succeeds, then the first one succeeds. This results in unupdated
  data.
  If the file is large, it will be locked for a while, preventing users from updating it. This happens everytime the
  file is saved, and can happen a lot with Excel 'auto save' feature.
  I use the modified time of a local file and I compare it to the modified time of an s3 file

* failures and shut-downs -
  I didn't test scenarios where a service shuts down, or there is no internet connection, in different times (on
  startup, when reading the file, when updating the db, etc.). Also, if updating the

* using shared classes -
  I use the same dto object and enum in both projects instead of extracting them to a single library and use that
  instead.

* large files -
  not tested on very large files.
  not tested on heavy write scenarios where a lot of players are being updated or added to the file. This can slow down
  response times for the endpoints.

* pagination -
  Didn't have time to add pagination to the reactive implementation

* batch updates -
  The current implementation uses a Queue Message per player that needs to be updated. The main issue with this is
  updating the db for each document, and not accessing the db once for a batch of documents.

* using a csv reader package -
  The csv format is simple so just splitting the file and accessing the fields by their index seemed to be enough.

* IDE warnings -
  Some warning there's no time left to handle.

* logs and alerts -
  I don't write logs to a service such as AWS Cloudwatch, and I'm not notified when an error occurs.

* memory and cpu testing, monitoring and alerting -
  I didn't check the cpu and memory consumption, especially of the file watcher, the synchronization with the db and
  getting the all players endpoint.

* scaling -
  I didn't try to run the REST service on multiple machines, or test the service with multiple clients.

* S3 file watcher -
  I use a WatchService for monitoring the players file when the file is local. The local implementation is commented out
  in the code. For monitoring the S3 file I can use AWS bucket notifications, when the event destination can be an SQS
  queue, or a lambda function that will send the event to our Rabbit queue.

* names -
  I guess the names of the projects could be better