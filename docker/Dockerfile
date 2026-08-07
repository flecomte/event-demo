# Stage 1: Cache Gradle dependencies
FROM gradle:9.6.1-jdk21-alpine AS cache
RUN mkdir -p /home/gradle/cache_home
ENV GRADLE_USER_HOME=/home/gradle/cache_home
COPY settings.gradle.kts build.gradle.kts gradle.properties /home/gradle/app/
COPY backend/build.gradle.kts /home/gradle/app/backend/
COPY shared/build.gradle.kts /home/gradle/app/shared/
WORKDIR /home/gradle/app
RUN gradle :backend:build -i -x check

# Stage 2: Build Application
FROM gradle:9.6.1-jdk21-alpine AS build
COPY --from=cache /home/gradle/cache_home /home/gradle/.gradle
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
# Build the fat JAR, Gradle also supports shadow
# and boot JAR by default.
RUN gradle :backend:buildFatJar --no-daemon

# Stage 3: Create the Runtime Image
FROM eclipse-temurin:21-jre-alpine AS runtime
EXPOSE 8080
RUN mkdir /app
COPY --from=build /home/gradle/src/backend/build/libs/*-all.jar /app/event-demo-all.jar
ENTRYPOINT ["java","-jar","/app/event-demo-all.jar"]