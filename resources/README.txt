1) Running the server
o Run server/bin/startup.bat or bin/startup.sh
o Open http://localhost:8063/
o The userid/password is bug4j/bug4j

2) Creating some bugs
o Go to demo/log4j/ and run "java -jar demo-log4j.jar"
o Go to demo/manual/ and run "java -jar demo-manual.jar"
o Go to http://localhost:8063/ and look at the bugs you have created.
Every time you run one of the demo programs you create a new hit on the same bug.
The statistics are update every hour. Admins (bug4j/bug4j) can force a refresh.

3) Instrumenting your application
I still need to add some example programs but for now please refer to the online documentation:
    http://www.bug4j.org/documentation/application-instrumentation

4) Configuring the bug4j server
bug4j runs on Apache Tomcat so please refer to the Tomcat documentation.
