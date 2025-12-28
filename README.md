# 📚 TP26 - Microservices Observable & Résilient

> **Auteur:** Karzouz Saad  
> **Date:** Décembre 2024  
> **Technologies:** Spring Boot 3.2, Resilience4j, MySQL, Docker, Actuator

---

## 🎯 Objectif du TP

Ce projet implémente deux microservices Spring Boot démontrant:

- ✅ **Observabilité** avec Spring Boot Actuator (health, metrics, prometheus)
- ✅ **Résilience** avec Resilience4j (Circuit Breaker, Retry, Fallback)
- ✅ **Gestion de profils** (dev avec H2, prod avec MySQL)
- ✅ **Dockerisation** avec multi-stage Dockerfile
- ✅ **Orchestration** avec Docker Compose
- ✅ **Wait Strategy** pour attendre MySQL
- ✅ **Scaling** avec plusieurs instances de book-service

---

## 📁 Architecture du Projet

```
tp26/
├── 📂 pricing-service/          # Microservice de pricing
│   ├── src/main/java/...
│   ├── src/main/resources/
│   │   └── application.yml
│   ├── Dockerfile
│   └── pom.xml
│
├── 📂 book-service/             # Microservice de gestion des livres
│   ├── src/main/java/...
│   ├── src/main/resources/
│   │   └── application.yml
│   ├── wait-for-db.sh
│   ├── Dockerfile
│   └── pom.xml
│
├── 📄 docker-compose.yml
├── 📄 init.sql
└── 📄 README.md
```

---

## 🔧 Configuration des Services

### Pricing Service (Port 8081)

| Endpoint | Méthode | Description |
|----------|---------|-------------|
| `/price/{bookId}` | GET | Récupère le prix d'un livre |
| `/toggleDown` | POST | Bascule l'état du service (UP/DOWN) |
| `/health-check` | GET | État de santé personnalisé |
| `/actuator/health` | GET | État de santé Actuator |
| `/actuator/metrics` | GET | Métriques |
| `/actuator/prometheus` | GET | Métriques Prometheus |

### Book Service (Port 8080)

| Endpoint | Méthode | Description |
|----------|---------|-------------|
| `/books` | GET | Liste tous les livres |
| `/books` | POST | Crée un nouveau livre |
| `/books/{id}` | GET | Récupère un livre |
| `/books/{id}` | PUT | Met à jour un livre |
| `/books/{id}` | DELETE | Supprime un livre |
| `/books/{id}/borrow` | POST | Emprunte un livre |
| `/books/available` | GET | Livres disponibles |
| `/actuator/health` | GET | État de santé |
| `/actuator/circuitbreakers` | GET | État des circuit breakers |

---

## 🚀 Lancement du Projet

### Option 1: Mode Développement (sans Docker)

```bash
# Terminal 1 - Pricing Service
cd pricing-service
mvn clean package -DskipTests
java -jar target/pricing-service.jar

# Terminal 2 - Book Service (avec H2)
cd book-service
mvn clean package -DskipTests
java -jar target/book-service.jar --spring.profiles.active=dev
```

**Console H2:** http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:booksdb`
- User: `sa`
- Password: (vide)

### Option 2: Mode Production (Docker Compose)

```bash
# Build et lancement
docker-compose build --no-cache
docker-compose up -d

# Vérifier les conteneurs
docker-compose ps

# Voir les logs
docker-compose logs -f

# Logs d'un service spécifique
docker-compose logs -f book-service
docker-compose logs -f pricing-service
```

### Option 3: Scaling (3 instances de book-service)

```bash
# Lancement avec scaling
docker-compose up --build -d
docker-compose up --scale book-service=3 -d

# Vérifier les instances
docker-compose ps
```

---

## 📸 Screenshots de Validation

### 1️⃣ Liste des livres
![Liste des livres](Screenshots/all%20books.png)

### 2️⃣ Créer un livre
![Créer un livre](Screenshots/Créer%20un%20livre.png)

### 3️⃣ Emprunter un livre (pricing UP)
![Emprunter un livre](Screenshots/Emprunter%20un%20livre.png)

### 4️⃣ Simuler une panne du pricing-service
![Simuler panne](Screenshots/Simuler%20une%20panne%20du%20pricing-service.png)

### 5️⃣ Emprunter un livre avec pricing DOWN (Fallback)
![Fallback activé](Screenshots/Emprunter%20un%20livre%20avec%20pricing%20DOWN.png)

### 6️⃣ Réactiver le pricing-service
![Réactiver pricing](Screenshots/Réactiver%20le%20pricing-service.png)

### 7️⃣ Vérifier le retour à la normale
![Retour normal](Screenshots/Vérifier%20le%20retour%20à%20la%20normale.png)

---

## ✅ Scénarios de Validation

### 1️⃣ Vérifier la santé des services

```bash
# Pricing Service
curl http://localhost:8081/actuator/health

# Book Service
curl http://localhost:8080/actuator/health
```

**Réponse attendue:**
```json
{
  "status": "UP",
  "components": {
    "circuitBreakers": { "status": "UP" },
    "db": { "status": "UP" },
    "diskSpace": { "status": "UP" }
  }
}
```

### 2️⃣ Créer un livre

```bash
curl -X POST http://localhost:8080/books \
  -H "Content-Type: application/json" \
  -d '{"title":"TP26 Book","author":"Karzouz Saad","stock":5}'
```

**Réponse attendue:**
```json
{
  "id": 6,
  "title": "TP26 Book",
  "author": "Karzouz Saad",
  "stock": 5
}
```

### 3️⃣ Lister les livres

```bash
curl http://localhost:8080/books
```

### 4️⃣ Emprunter un livre (service pricing UP)

```bash
curl -X POST http://localhost:8080/books/1/borrow
```

**Réponse attendue:**
```json
{
  "bookId": 1,
  "title": "Clean Code",
  "author": "Robert C. Martin",
  "success": true,
  "status": "BORROWED",
  "previousStock": 5,
  "remainingStock": 4,
  "price": 19.99,
  "currency": "EUR",
  "pricingServiceAvailable": true
}
```

### 5️⃣ Simuler une panne du pricing-service

```bash
# Mettre pricing-service en panne
curl -X POST http://localhost:8081/toggleDown
```

**Réponse:**
```json
{
  "serviceUp": false,
  "status": "DOWN",
  "message": "Service is now DOWN - pricing requests will fail (simulated failure)"
}
```

### 6️⃣ Emprunter un livre avec fallback

```bash
curl -X POST http://localhost:8080/books/1/borrow
```

**Réponse attendue (avec fallback):**
```json
{
  "bookId": 1,
  "title": "Clean Code",
  "success": true,
  "status": "BORROWED",
  "previousStock": 4,
  "remainingStock": 3,
  "price": 0.0,
  "currency": "EUR",
  "pricingServiceAvailable": false,
  "priceNote": "Fallback price used - pricing service was unavailable"
}
```

### 7️⃣ Vérifier le Circuit Breaker

```bash
# État des circuit breakers
curl http://localhost:8080/actuator/circuitbreakers

# Métriques détaillées
curl http://localhost:8080/actuator/metrics/resilience4j.circuitbreaker.state
```

### 8️⃣ Réactiver le pricing-service

```bash
curl -X POST http://localhost:8081/toggleDown
```

**Réponse:**
```json
{
  "serviceUp": true,
  "status": "UP",
  "message": "Service is now UP - pricing requests will succeed"
}
```

### 9️⃣ Vérifier le retour à la normale

```bash
# Attendre quelques secondes que le circuit se ferme
sleep 15

# Emprunter un livre - le prix devrait être récupéré normalement
curl -X POST http://localhost:8080/books/2/borrow
```

---

## 📊 Logs Attendus

### Logs normaux (pricing UP)

```
BookService  : === BORROW OPERATION START for bookId=1 ===
PricingClient: Calling pricing-service: GET http://pricing-service:8081/price/1
PricingClient: Received price 19.99 from pricing-service for bookId=1
BookService  : Stock decremented for book 1: 5 -> 4
BookService  : === BORROW OPERATION SUCCESS for bookId=1 ===
```

### Logs avec fallback (pricing DOWN)

```
BookService  : === BORROW OPERATION START for bookId=1 ===
PricingClient: Calling pricing-service: GET http://pricing-service:8081/price/1
PricingClient: Error calling pricing-service for bookId=1: Connection refused
PricingClient: === FALLBACK TRIGGERED ===
PricingClient: pricing-service unavailable for bookId=1, using fallback price=0.0
BookService  : Fallback price used - pricing-service was unavailable
BookService  : === BORROW OPERATION SUCCESS for bookId=1 ===
```

### Logs Circuit Breaker

```
Resilience4j: CircuitBreaker 'pricing' state transition: CLOSED -> OPEN
Resilience4j: CircuitBreaker 'pricing' recorded a failed call
Resilience4j: CircuitBreaker 'pricing' is OPEN and not allowing calls
...
Resilience4j: CircuitBreaker 'pricing' state transition: OPEN -> HALF_OPEN
Resilience4j: CircuitBreaker 'pricing' state transition: HALF_OPEN -> CLOSED
```

---

## ⚙️ Configuration Resilience4j

```yaml
resilience4j:
  circuitbreaker:
    instances:
      pricing:
        slidingWindowSize: 10          # Fenêtre de 10 appels
        minimumNumberOfCalls: 5        # Minimum 5 appels avant évaluation
        failureRateThreshold: 50       # Ouverture à 50% d'échecs
        waitDurationInOpenState: 10s   # Durée en état OPEN
        permittedNumberOfCallsInHalfOpenState: 3  # Appels en HALF_OPEN
  
  retry:
    instances:
      pricing:
        maxAttempts: 3                 # 3 tentatives maximum
        waitDuration: 1s               # 1s entre les tentatives
        enableExponentialBackoff: true # Backoff exponentiel
```

---

## 🐛 Problèmes Courants et Solutions

### Erreur: "MySQL not ready"

```bash
# Vérifier le healthcheck MySQL
docker-compose logs mysql

# Relancer book-service
docker-compose restart book-service
```

### Erreur: "Connection refused to pricing-service"

```bash
# Vérifier que pricing-service est UP
curl http://localhost:8081/actuator/health

# Vérifier les logs
docker-compose logs pricing-service
```

### Erreur: "Port already in use"

```bash
# Trouver le processus
netstat -ano | findstr :8080

# Arrêter Docker Compose
docker-compose down
```

### Circuit Breaker toujours OPEN

```bash
# Vérifier l'état
curl http://localhost:8080/actuator/circuitbreakers

# Le circuit se fermera automatiquement après waitDurationInOpenState (10s)
# et quelques appels réussis en HALF_OPEN
```

---

## 🛑 Arrêt du Projet

```bash
# Arrêter tous les conteneurs
docker-compose down

# Arrêter et supprimer les volumes (données MySQL)
docker-compose down -v

# Supprimer les images
docker-compose down --rmi all
```

---

## 📦 Résumé des Ports

| Service | Port Interne | Port Exposé |
|---------|--------------|-------------|
| MySQL | 3306 | 3306 |
| Pricing Service | 8081 | 8081 |
| Book Service | 8080 | 8080-8089* |

*Avec scaling, les ports 8080-8089 sont utilisés pour les différentes instances.

---

## 📝 Checklist de Validation

- [x] MySQL démarre et est accessible
- [x] pricing-service démarre et répond sur `/actuator/health`
- [x] book-service démarre et se connecte à MySQL
- [x] Création de livre fonctionne (POST /books)
- [x] Liste des livres fonctionne (GET /books)
- [x] Emprunt avec pricing UP retourne le prix correct
- [x] Toggle pricing DOWN fonctionne
- [x] Emprunt avec pricing DOWN utilise le fallback (price=0.0)
- [x] Circuit Breaker s'ouvre après plusieurs échecs
- [x] Toggle pricing UP et le circuit se referme
- [x] Scaling à 3 instances fonctionne

---

**🎉 Bon TP !**
