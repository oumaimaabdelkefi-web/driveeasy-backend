# DriveEasy Backend - Architecture Microservices

> **Plateforme de Gestion de Location de VÃ©hicules**
> **Module** : DÃ©veloppement d'applications web et multimÃ©dia (J2EE, .Net..)
> **FiliÃ¨re** : SESAME â€” Licence en Informatique et MultimÃ©dia (LIM3 B/C)
> **Enseignant** : Karim Ben Romdhane

---

## 1. Description du Projet

**DriveEasy** est une plateforme moderne et scalable de location de vÃ©hicules de courte et moyenne durÃ©e conÃ§ue selon une architecture microservices avec **Spring Boot 3.x** et **Spring Cloud**.

Elle remplace une architecture monolithique pour offrir aux agences de location :
- La gestion complÃ¨te du parc de vÃ©hicules (caractÃ©ristiques, disponibilitÃ©, maintenance).
- La gestion de la clientÃ¨le, des coordonnÃ©es et de la validitÃ© du permis de conduire.
- Le cycle de vie complet des locations (rÃ©servation, simulation de prix, dÃ©part, retour avec pÃ©nalitÃ©s de retard, annulation).
- Le calcul dynamique des prix avec application des remises fidÃ©litÃ© (STANDARD : 0%, FIDELE : 5%, VIP : 10%).
- Des indicateurs et statistiques pour les gestionnaires d'agences (CA mensuel, CA total, taux d'occupation, top vÃ©hicules louÃ©s).
- **Stockage en mÃ©moire vive (RAM)** conformÃ©ment Ã  la consigne stricte du cahier des charges (aucun SGBD persistant, collections concurrentes thread-safe `ConcurrentHashMap`).

---

## 2. Architecture Globale et Ports

| Composant | Technologie | Port | RÃ´le |
|---|---|---|---|
| **Service Registry** | Spring Cloud Netflix Eureka | **8761** | Annuaire des services et dÃ©couverte dynamique |
| **Config Server** | Spring Cloud Config Server | **8888** | Configuration centralisÃ©e (profil native & git) |
| **vehicule-service** | Spring Boot Web 3.x | **8081** | Gestion du parc de vÃ©hicules et disponibilitÃ©s |
| **client-service** | Spring Boot Web 3.x | **8082** | Gestion des clients, de l'Ã©ligibilitÃ© et fidÃ©litÃ© |
| **location-service** | Spring Boot Web 3.x + OpenFeign | **8083** | Orchestrateur mÃ©tier, cycle de vie, calculs et stats |
| **API Gateway** | Spring Cloud Gateway | **8080** | Point d'entrÃ©e unique, routage `lb://`, filtre custom, CORS |
| **Zipkin (Docker)** | OpenZipkin | **9411** | TraÃ§age distribuÃ© des requÃªtes inter-services |

---

## 3. PrÃ©requis

- **Java JDK 17 ou 21** installÃ© (`java -version`).
- **Apache Maven 3.9+** installÃ© (`mvn -version`).
- **Docker & Docker Compose** (pour le conteneur Zipkin).
- **Node.js 18+ / npm** (pour le frontend Angular).

---

## 4. Instructions de Lancement Pas Ã  Pas

### Ordre de dÃ©marrage recommandÃ© :
1. **Zipkin (Monitoring)** (Docker)
2. **Service Registry (Eureka)** (Port 8761)
3. **Config Server** (Port 8888)
4. **vehicule-service** (Port 8081)
5. **client-service** (Port 8082)
6. **location-service** (Port 8083)
7. **api-gateway** (Port 8080)

---

### Ã‰tape 1 : DÃ©marrer Zipkin (Docker)
Ã€ la racine de `driveeasy-backend` :
```bash
docker compose up -d
```
- Interface Zipkin accessible sur : [http://localhost:9411](http://localhost:9411)

---

### Ã‰tape 2 : Compiler et Packager tous les Microservices
Ã€ la racine du dossier `driveeasy-backend` :
```bash
mvn clean package -DskipTests
```
Cette commande unique gÃ©nÃ¨re automatiquement les fichiers JAR exÃ©cutables pour chacun des 6 services dans leurs rÃ©pertoires `target/` respectifs.

---

### Ã‰tape 3 : DÃ©marrer les Microservices

Ouvrez des terminaux distincts (ou utilisez un script) :

#### Terminal 1 â€” Service Registry (Eureka)
```bash
cd service-registry
mvn spring-boot:run
# Ou : java -jar target/service-registry-1.0.0.jar
```
- Dashboard Eureka : [http://localhost:8761](http://localhost:8761)

#### Terminal 2 â€” Config Server
```bash
cd config-server
mvn spring-boot:run
# Ou : java -jar target/config-server-1.0.0.jar
```
- VÃ©rification : [http://localhost:8888/vehicule-service/default](http://localhost:8888/vehicule-service/default)

#### Terminal 3 â€” vehicule-service
```bash
cd vehicule-service
mvn spring-boot:run
# Ou : java -jar target/vehicule-service-1.0.0.jar
```
- Actuator : [http://localhost:8081/actuator/health](http://localhost:8081/actuator/health)

#### Terminal 4 â€” client-service
```bash
cd client-service
mvn spring-boot:run
# Ou : java -jar target/client-service-1.0.0.jar
```
- Actuator : [http://localhost:8082/actuator/health](http://localhost:8082/actuator/health)

#### Terminal 5 â€” location-service
```bash
cd location-service
mvn spring-boot:run
# Ou : java -jar target/location-service-1.0.0.jar
```
- Actuator : [http://localhost:8083/actuator/health](http://localhost:8083/actuator/health)

#### Terminal 6 â€” API Gateway
```bash
cd api-gateway
mvn spring-boot:run
# Ou : java -jar target/api-gateway-1.0.0.jar
```
- Point d'accÃ¨s unique : [http://localhost:8080](http://localhost:8080)

---

## 5. Routes de l'API Gateway

Toutes les requÃªtes passent par la Gateway (port 8080) qui rÃ©Ã©crit les chemins (`StripPrefix=1`) et Ã©quilibre la charge via Eureka :

| Chemin ExposÃ© (Gateway) | Service Cible | MÃ©thodes SupportÃ©es |
|---|---|---|
| `/api/vehicules/**` | `vehicule-service` (8081) | `GET`, `POST`, `PUT`, `DELETE`, `PATCH` |
| `/api/clients/**` | `client-service` (8082) | `GET`, `POST`, `PUT`, `DELETE`, `PATCH` |
| `/api/locations/**` | `location-service` (8083) | `GET`, `POST`, `PATCH` |

### Filtre PersonnalisÃ© (Gateway) :
Chaque rÃ©ponse traversant la passerelle inclut automatiquement :
- `X-DriveEasy-Gateway: Processed`
- `X-Response-Time-Ms: <duree_en_millisecondes>`

---

## 6. Jeux de DonnÃ©es Initiaux (ChargÃ©s en RAM)

### VÃ©hicules (`vehicule-service`) :
1. **Peugeot 208** (220-TN-1001) â€” Essence, Manuelle, 85.00 DT/jour, `DISPONIBLE`
2. **Renault Clio 5** (225-TN-2002) â€” Essence, Manuelle, 80.00 DT/jour, `DISPONIBLE`
3. **Volkswagen Golf 8** (218-TN-3003) â€” Diesel, Automatique, 120.00 DT/jour, `LOUE`
4. **Toyota Yaris Cross** (230-TN-4004) â€” Hybride, Automatique, 110.00 DT/jour, `DISPONIBLE`
5. **Tesla Model 3** (235-TN-5005) â€” Ã‰lectrique, Automatique, 160.00 DT/jour, `DISPONIBLE`
6. **Hyundai Tucson** (210-TN-6006) â€” Diesel, Automatique, 130.00 DT/jour, `MAINTENANCE`
7. **Mercedes-Benz Classe A** (240-TN-7007) â€” Essence, Automatique, 180.00 DT/jour, `DISPONIBLE`

### Clients (`client-service`) :
1. **Amine Mansour** (CIN: 09876543) â€” Ã‚ge : 28 ans, CatÃ©gorie : `FIDELE` (Remise 5%), Ã‰ligible : **OUI**
2. **Sarra Ben Ali** (CIN: 12345678) â€” Ã‚ge : 34 ans, CatÃ©gorie : `VIP` (Remise 10%), Ã‰ligible : **OUI**
3. **Karim Trabelsi** (CIN: 08765432) â€” Ã‚ge : 24 ans, CatÃ©gorie : `STANDARD`, Ã‰ligible : **OUI**
4. **Nour Jaziri** (CIN: 07654321) â€” Ã‚ge : 19 ans (< 21 ans) â€” Ã‰ligible : **NON** *(Test d'Ã©ligibilitÃ© Ã¢ge)*
5. **Mohamed Dridi** (CIN: 05432109) â€” Permis expirÃ© en 2023 â€” Ã‰ligible : **NON** *(Test d'Ã©ligibilitÃ© permis)*

---

## 7. Exemples de RequÃªtes de Test (Postman / cURL)

### Tester via la Gateway (`http://localhost:8080`) :

#### 1. Liste des vÃ©hicules disponibles
```bash
curl -X GET "http://localhost:8080/api/vehicules/disponibles"
```

#### 2. VÃ©rifier l'Ã©ligibilitÃ© d'un client
```bash
# Client Ã©ligible (ID 1)
curl -X GET "http://localhost:8080/api/clients/1/eligibilite"

# Client mineur non Ã©ligible (ID 4)
curl -X GET "http://localhost:8080/api/clients/4/eligibilite"
```

#### 3. Simuler le prix d'une location
```bash
curl -X POST "http://localhost:8080/api/locations/simulation" \
  -H "Content-Type: application/json" \
  -d '{"clientId": 1, "vehiculeId": 1, "dateDebut": "2026-11-20", "dateFinPrevue": "2026-11-25"}'
```

#### 4. CrÃ©er une rÃ©servation
```bash
curl -X POST "http://localhost:8080/api/locations" \
  -H "Content-Type: application/json" \
  -d '{"clientId": 1, "vehiculeId": 1, "dateDebut": "2026-11-20", "dateFinPrevue": "2026-11-25"}'
```

#### 5. DÃ©marrer une location (Statut passe Ã  EN_COURS, vÃ©hicule passe Ã  LOUE)
```bash
curl -X PATCH "http://localhost:8080/api/locations/3/depart"
```

#### 6. Retourner un vÃ©hicule (calcul final, pÃ©nalitÃ©s, remise Ã  DISPONIBLE)
```bash
curl -X PATCH "http://localhost:8080/api/locations/1/retour" \
  -H "Content-Type: application/json" \
  -d '{"kmRetour": 41850, "dateRetourEffectif": "2026-11-18"}'
```

#### 7. Consulter les statistiques d'agence
```bash
curl -X GET "http://localhost:8080/api/locations/statistiques"
```
