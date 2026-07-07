# Etapa 1: Construcción (Build)
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app

# Copiamos solo lo necesario para descargar dependencias (esto se cachea)
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

RUN chmod +x ./mvnw
# Descarga las dependencias sin compilar el código aún
RUN ./mvnw dependency:go-offline -B

# Ahora copiamos el código fuente y compilamos
COPY src src
RUN ./mvnw clean package -DskipTests

# Etapa 2: Ejecución (Run)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Exponemos el puerto (informativo, Render usa la variable PORT)
EXPOSE 8080

# Copiamos el JAR (ajusta el nombre si en tu pom.xml el artifactId es distinto)
COPY --from=build /app/target/*.jar app.jar

# Ejecución optimizada para contenedores y compatible con el puerto de Render
ENTRYPOINT ["java", "-Dserver.port=${PORT:8080}", "-jar", "app.jar"]