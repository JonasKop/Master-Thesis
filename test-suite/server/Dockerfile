FROM gradle:jdk15 as build
COPY build.gradle gradle.properties settings.gradle ./
COPY src ./src
COPY gradle ./gradle
COPY .gradle ./.gradle
RUN gradle clean build --no-daemon --stacktrace

FROM openjdk:15-jdk-alpine as prod
WORKDIR /app
COPY --from=build /home/gradle/build/libs/*.jar ./app.jar
CMD [ "java", "-jar", "-Djava.security.egd=file:/dev/./urandom", "./app.jar" ]
