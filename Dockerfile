FROM openjdk:17-jdk
COPY target/*.jar /app.jar
CMD java -jar app.jar
