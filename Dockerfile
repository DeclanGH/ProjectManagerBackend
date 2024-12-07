FROM openjdk:17-jdk-slim

# Set
WORKDIR /app

# Add
VOLUME /tmp

# Copy
COPY target/ProjectManagerBackend-0.0.1-SNAPSHOT.jar projectmanagerbackendprod.jar

# Expose
EXPOSE 8080

# Run
ENTRYPOINT ["java", "-jar", "projectmanagerbackendprod.jar"]
