FROM maven:3.9.9-eclipse-temurin-21

WORKDIR /app

COPY ./application/target/application*.jar application.jar

EXPOSE 8080

# Command to run the application with Spring Boot
CMD ["java", "-jar", "./application.jar"]
