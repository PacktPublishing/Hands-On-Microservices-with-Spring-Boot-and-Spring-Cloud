FROM openjdk:11.0.3-jre-slim

EXPOSE 9999

ADD ./build/libs/*.jar app.jar

ENTRYPOINT ["java","-jar","/app.jar"]
