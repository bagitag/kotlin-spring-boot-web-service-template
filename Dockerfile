FROM eclipse-temurin:21-jre-alpine
LABEL maintainer=bagitagab
EXPOSE 8080
RUN addgroup --system appuser && adduser -S appuser -G appuser
WORKDIR /app
ARG JAR_FILE=template-project-web/target/*-exec.jar
COPY ${JAR_FILE} app.jar
USER appuser:appuser
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]
