FROM openjdk:17

ARG JAR_FILE=build/libs/*.jar

COPY ${JAR_FILE} eureka.jar

EXPOSE 8761

ENTRYPOINT ["java", "-jar", "-Duser.timezone=Asia/Seoul", "/eureka.jar"]