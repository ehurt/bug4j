1) Running the server
o Run server/bin/startup.bat or bin/startup.sh
o Open http://localhost:8063/
o The userid/password is bug4j/bug4j

2) Instrumenting your application
I still need to add some example programs but for now please refer to the online documentation:
    http://www.bug4j.org/documentation/application-instrumentation

3) Configuring the bug4j server
bug4j runs on Apache Tomcat so please refer to the Tomcat documentation.
3.1) User configuration
Edit server/conf/tomcat-users.xml.
The default username/password is bug4j/bug4j but bug4j saves user preferences so you should use a different userid for each real user of the bug4j UI.

