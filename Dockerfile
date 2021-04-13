FROM maven:3.6.3-openjdk-11-slim as MAVEN_TOOL_CHAIN
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
