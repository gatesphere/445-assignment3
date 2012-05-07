# CSC 445 - Assignment 3

## Protocol notes
The nodes will support the following types of commands:

### GET commands
A GET command is a query.  The query string will be formatted as follows:

    GET <num> <field_1>='<value_1>', <field_1>='<value_2>', ... , <field_n>='<value_n>'
    
Where `<num>` is either an integer or the string literal `ALL`.  The leader node will
send back a serialized collection consisting of no more than `<num>` Music objects which 
match the given field qualifiers.  Examples are as follows:

    GET 10 ARTIST='Foo Fighters'
    GET ALL TITLE='Fiddle and the Drum', GENRE='Folk'
    GET 3 YEAR='2006'
    
A node recieving a GET command from a client should forward it to the other nodes,
replacing any `<num>` with `ALL`.  The other nodes will reply to the leader node
with complete collections, which the leader node will check for consensus on at
most `<num>` items, returning that collection to the client.

### PUT commands
A PUT command is the command to store a new record to the nodes.  The command is
formatted as follows:

    PUT <timestamp>,<id>, <artist>, <album_artist>, <album>, <track>, <title>, <year>, <genre>, <track_length>
    
Where each field is it's corresponding data.  `<id>` should be filled out with a UUID, generated
by `UUID.randomUUID()`.  `<timestamp>` should be filled out with a timestamp, generated by
`System.nanoTime() + (Math.random()*10000000000)`.  In the case that two requests are filed
with the same UUID, the one with the larger timestamp is chosen as the winner.

### KILL commands
A client can send a KILL command to a node to simulate network failure.  The kill
command is formatted as follows:

    KILL

A node recieving a KILL command should immediately cut network communications
and cease operations.

## Flow diagrams

### GET
````
key: c  = client
     n1 = node 1
     n2 = node 2
     n3 = node 3
     (n1 is chosen as the leader node at random by the client)


   c               n1                   n2                   n3
   send req
                   recieve req
                   forward req ->n2,n3
                   perform query        recieve req          recieve req
                                        perform query        perform query
                                        send results ->n1    send results ->n1
                   recieve results ->n2
                   recieve results ->n3
                   consensus
                   limit to <num>
                   send results ->c
  recieve results
  map
  reduce
````

### PUT
````
key: c  = client
     n1 = node 1
     n2 = node 2
     n3 = node 3
     (n1 is chosen as the leader node at random by the client)


   c               n1                   n2                   n3
   send put
                   recieve put
                   forward put ->n2,n3
                   store put            recieve put          recieve put
                                        store put            store put
````

### KILL
````
key: c  = client
     n1 = node 1
     n2 = node 2
     n3 = node 3
     (n1 is chosen as the dying node at random by the client)

   c               n1                   n2                   n3
   send kill ->n1  
                   recieve kill
                   die
                   x
                   x
                   x
                   x
````

## To do list:

  * Client
    * test
  * Node
    * test querying
  * MusicObject
    * test querying
  * NodeServer
    * Concurrent connection framework
    * Request parsing
      * Querying
      * Leader notification
      * Limiting results
    * Store parsing
      * Storing the object
    * Kill command
      * Exit, immediately