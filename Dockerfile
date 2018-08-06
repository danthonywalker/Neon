FROM openjdk
WORKDIR /etc/neon
EXPOSE 8080

ARG NEON_JAR
COPY ${NEON_JAR} /opt/neon/neon.jar
ENTRYPOINT ["java", "-jar", "/opt/neon/neon.jar"]
