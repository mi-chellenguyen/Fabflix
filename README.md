# Fabflix

Fabflix is a web appplication that allows a user to search, browse, and purchase movies. This was a class project for CS 122B at University of California Irvine taken during Winter of 2019. It was developed using Java EE framework(Servlets, JSP, JSTL, EL, JDBC) along with front-end technologies(HTML,CSS,Javascript,jQuery,AJAX,BootStrap). This application used to be hosted and deployed on AWS platform but has now been shut down.

**Developed by:** Michelle Nguyen, Katrina Dang

## Performance Optimization Techniques 

### Used SAX parser
This is an event based XML Parsing, and it parses XML file step by step so it is more suitable for large XML Files.
It does not require loading the whole XML file like a DOM parser which requires having a lot of memory, and it 
can read a big XML file in small parts. Since the XML files being parsed are large files, using a SAX parser 
will minimize memory consumption. Inserting all of the data from these Hash Maps will be done all at once using 
SQL statement LOAD DATA INFILE. 

### Used LOAD DATA INFILE instead of normal INSERT statements
LOAD DATA INFILE is a highly optimized MySQL-specific statement that directly inserts data into a table from a CSV/TSV file.

### Connection pooling 
With Connection pooling it allows connections to be reused without having to make new connections each time a connection is requested. When there is a connection request, it takes a connection from the pool that is pre-created and does not need to be established again. Thus the time it takes to establish and free connections is saved.

### Implements MySQL master/slave
The web app reads and writes to MySQL Master and only reads to MySQL slave. This app routes write requests to Master while allowing read requests to either Master or Slave.
This is helpful for when a site receives a lot of traffic. It ensures that the database will not be overloaded with reading and writing requests.