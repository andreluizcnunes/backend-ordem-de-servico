FROM eclipse-temurin:25-jdk AS construcao
WORKDIR /construcao
COPY .mvn .mvn
COPY mvnw pom.xml lombok.config ./
RUN --mount=type=cache,target=/root/.m2 ./mvnw -B -q dependency:go-offline
COPY src/main src/main
RUN --mount=type=cache,target=/root/.m2 ./mvnw -B -q -Dmaven.test.skip=true package \
    && java -Djarmode=tools -jar target/backend-ordem-de-servico-0.0.1-SNAPSHOT.jar extract \
        --layers --launcher --destination extraido

FROM eclipse-temurin:25-jre
RUN groupadd --system aplicacao && useradd --system --gid aplicacao --no-create-home aplicacao
WORKDIR /aplicacao
COPY --from=construcao /construcao/extraido/dependencies/ ./
COPY --from=construcao /construcao/extraido/spring-boot-loader/ ./
COPY --from=construcao /construcao/extraido/snapshot-dependencies/ ./
COPY --from=construcao /construcao/extraido/application/ ./
USER aplicacao
EXPOSE 8080
HEALTHCHECK --interval=15s --timeout=5s --start-period=40s --retries=5 \
    CMD ["bash", "-c", "exec 3<>/dev/tcp/127.0.0.1/8080 && printf 'GET /actuator/health/readiness HTTP/1.0\\r\\nHost: localhost\\r\\n\\r\\n' >&3 && grep -q '\"UP\"' <&3"]
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "org.springframework.boot.loader.launch.JarLauncher"]
