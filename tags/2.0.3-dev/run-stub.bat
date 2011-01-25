@echo off

set CP=lib\jetty-6.1.7.jar;lib\servlet-api-2.5-6.1.7.jar;lib\jetty-util-6.1.7.jar;classes

java -cp "%CP%" com.cordys.coe.httpconnector.jetty.WebServerStub