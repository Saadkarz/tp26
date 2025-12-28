# ===========================================
# Dockerfile Multi-Stage pour Book Service
# TP26 - Microservice Observable & Résilient
# ===========================================

# Stage 1: Build avec Maven
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

# Copier les fichiers POM et télécharger les dépendances (cache layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copier le code source et construire
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Runtime avec JRE
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Installer les outils nécessaires pour le wait script
RUN apk add --no-cache bash netcat-openbsd

# Créer un utilisateur non-root pour la sécurité
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# Copier le script wait-for-db
COPY wait-for-db.sh /app/wait-for-db.sh
RUN chmod +x /app/wait-for-db.sh

# Copier le JAR depuis le stage de build
COPY --from=builder /app/target/book-service.jar app.jar

# Changer le propriétaire des fichiers
RUN chown -R appuser:appgroup /app

# Utiliser l'utilisateur non-root
USER appuser

# Port exposé
EXPOSE 8080

# Variables d'environnement par défaut
ENV JAVA_OPTS="-Xmx512m -Xms256m"
ENV SPRING_PROFILES_ACTIVE=prod
ENV DB_HOST=mysql
ENV DB_PORT=3306
ENV MAX_RETRIES=30
ENV RETRY_INTERVAL=2

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=90s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Point d'entrée avec wait script
ENTRYPOINT ["/app/wait-for-db.sh"]
