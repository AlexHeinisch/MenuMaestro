FROM maven:3.9.9-eclipse-temurin-21

WORKDIR /app

COPY . .

RUN mvn install -B -DskipTests=true

EXPOSE 8080

# Command to run the application with Spring Boot
CMD ["mvn", "-f", "application/pom.xml", "spring-boot:run"]
