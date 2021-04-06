################
# Build Client #
################
FROM node:alpine3.13 as build-client

WORKDIR /app
COPY ./client/package.json ./client/yarn.lock ./
COPY ./client/public ./public
COPY ./client/src ./src
RUN yarn
RUN yarn build


################
# Build server #
################
FROM gradle:jdk15 as build-server
WORKDIR /app
COPY ./server/build.gradle ./server/gradle.properties ./server/settings.gradle ./
COPY ./server/src ./src
COPY ./server/gradle ./gradle
COPY ./server/.gradle ./.gradle
RUN gradle clean build --no-daemon --stacktrace


###############
# Final image #
###############
FROM openjdk:15-jdk-alpine
WORKDIR /app
ENV CLIENT_PATH=/www/client
COPY --from=build-server /app/build/libs/*.jar ./app.jar
COPY --from=build-client /app/build $CLIENT_PATH
CMD [ "java", "-jar", "-Djava.security.egd=file:/dev/./urandom", "./app.jar" ]
