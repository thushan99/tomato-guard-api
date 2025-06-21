FROM openjdk:21
LABEL maintainer="m.t.miuranga@gmail.com"
WORKDIR /app
COPY target/sample-app.jar /app/sample-app.jar
ENTRYPOINT ["java","-jar","sample-app.jar"]