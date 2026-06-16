# 🏦 stock-info-service

A production-ready Spring Boot microservice that retrieves real-time stock market details by **ticker symbol** (e.g., `AAPL`) or **company name** (e.g., `Apple`) using the [Alpha Vantage API](https://www.alphavantage.co), with an intelligent mock data fallback engine for 25+ popular stocks.

---

## ✨ Features

- **Flexible input**: accepts ticker symbols OR company names
- **Live data**: integrates with Alpha Vantage free API (GLOBAL_QUOTE + SYMBOL_SEARCH)
- **Smart fallback**: built-in mock data engine for 25+ stocks (AAPL, MSFT, TSLA, NVDA, META, etc.)
- **Rich response**: price, open/high/low, volume, change %, market cap, P/E ratio, 52-week range
- **Two endpoints**: POST with JSON body + GET with path variable
- **Validation**: request body validated with proper error messages
- **Cloud-ready**: Spring Actuator health checks, Docker multi-stage build, environment-variable config
- **Security**: non-root Docker user, container-aware JVM settings

---

## 🚀 Quick Start

### Option 1: Run Locally (without Docker)
```bash
# 1. Build the project
mvn clean package -DskipTests

# 2. Run the service
mvn spring-boot:run

# Service will be available at http://localhost:8080
```

### Option 2: Run with Docker (Recommended)
```bash
# 1. Build and start (first time takes ~2-3 minutes to download layers)
docker-compose up -d --build

# 2. Check service is healthy
docker-compose ps
docker-compose logs -f stock-info-service

# 3. Stop the service
docker-compose down
```

### Option 3: Run with your own API key
```bash
# Set your Alpha Vantage key (free at https://www.alphavantage.co/support/#api-key)
# Edit .env file:
ALPHA_VANTAGE_API_KEY=YOUR_KEY_HERE

# Then start:
docker-compose up -d --build
```

---

## 📡 API Endpoints

### POST /api/v1/stocks/lookup
Look up stock details by ticker symbol or company name.

**Request:**
```http
POST http://localhost:8080/api/v1/stocks/lookup
Content-Type: application/json

{
  "query": "AAPL"
}
```

**Response:**
```json
{
  "symbol": "AAPL",
  "companyName": "Apple Inc.",
  "exchange": "NASDAQ",
  "currency": "USD",
  "price": 189.52,
  "open": 187.10,
  "high": 190.43,
  "low": 186.12,
  "previousClose": 187.20,
  "change": 2.32,
  "changePercent": "1.2393%",
  "volume": 54831200,
  "marketCap": "$2.89T",
  "peRatio": "31.2",
  "week52High": 210.50,
  "week52Low": 164.08,
  "latestTradingDay": "2026-06-15",
  "dataSource": "Mock Data Engine (Alpha Vantage fallback)"
}
```

---

### GET /api/v1/stocks/lookup/{symbol}
Convenience GET endpoint for quick lookups.

```http
GET http://localhost:8080/api/v1/stocks/lookup/TSLA
```

---

### GET /actuator/health
Health check endpoint for load balancers and container orchestrators.

```http
GET http://localhost:8080/actuator/health
```

**Response:**
```json
{
  "status": "UP",
  "components": { "diskSpace": {...}, "ping": {...} }
}
```

---

## 🧪 Sample curl Commands

```bash
# 1. By ticker symbol
curl -X POST http://localhost:8080/api/v1/stocks/lookup \
  -H "Content-Type: application/json" \
  -d '{"query": "AAPL"}'

# 2. By company name
curl -X POST http://localhost:8080/api/v1/stocks/lookup \
  -H "Content-Type: application/json" \
  -d '{"query": "Microsoft"}'

# 3. GET convenience endpoint
curl http://localhost:8080/api/v1/stocks/lookup/NVDA

# 4. IBM (live data with demo key)
curl -X POST http://localhost:8080/api/v1/stocks/lookup \
  -H "Content-Type: application/json" \
  -d '{"query": "IBM"}'

# 5. Test validation (empty query)
curl -X POST http://localhost:8080/api/v1/stocks/lookup \
  -H "Content-Type: application/json" \
  -d '{"query": ""}'
```

---

## 📊 Supported Stocks (Mock Engine)

| Symbol | Company | Exchange |
|--------|---------|----------|
| AAPL | Apple Inc. | NASDAQ |
| MSFT | Microsoft Corporation | NASDAQ |
| GOOGL | Alphabet Inc. | NASDAQ |
| AMZN | Amazon.com Inc. | NASDAQ |
| NVDA | NVIDIA Corporation | NASDAQ |
| TSLA | Tesla Inc. | NASDAQ |
| META | Meta Platforms Inc. | NASDAQ |
| JPM | JPMorgan Chase & Co. | NYSE |
| V | Visa Inc. | NYSE |
| NFLX | Netflix Inc. | NASDAQ |
| IBM | International Business Machines | NYSE |
| AMD | Advanced Micro Devices | NASDAQ |
| DIS | The Walt Disney Company | NYSE |
| SHOP | Shopify Inc. | NYSE |
| UBER | Uber Technologies Inc. | NYSE |
| COIN | Coinbase Global Inc. | NASDAQ |
| ... and more | | |

> **Note**: `IBM` returns **live data** from Alpha Vantage even with the `demo` API key.

---

## ☁️ Cloud Deployment

### AWS ECS / Fargate
1. Push image to ECR:
   ```bash
   aws ecr get-login-password | docker login --username AWS --password-stdin <account>.dkr.ecr.<region>.amazonaws.com
   docker tag stock-info-service:latest <account>.dkr.ecr.<region>.amazonaws.com/stock-info-service:latest
   docker push <account>.dkr.ecr.<region>.amazonaws.com/stock-info-service:latest
   ```
2. Create ECS Task Definition with `ALPHA_VANTAGE_API_KEY` as an environment variable (use AWS Secrets Manager for security).
3. Create ECS Service with Application Load Balancer.
4. Health check path: `/actuator/health`

### Google Cloud Run
```bash
gcloud run deploy stock-info-service \
  --image=gcr.io/PROJECT_ID/stock-info-service:latest \
  --port=8080 \
  --set-env-vars=ALPHA_VANTAGE_API_KEY=YOUR_KEY
```

### Azure Container Apps
```bash
az containerapp create \
  --name stock-info-service \
  --image stock-info-service:latest \
  --target-port 8080 \
  --env-vars ALPHA_VANTAGE_API_KEY=YOUR_KEY
```

### Kubernetes (any cloud)
```bash
kubectl create deployment stock-info-service --image=stock-info-service:latest
kubectl expose deployment stock-info-service --port=80 --target-port=8080 --type=LoadBalancer
kubectl set env deployment/stock-info-service ALPHA_VANTAGE_API_KEY=YOUR_KEY
```

---

## ⚙️ Configuration Reference

| Property | Environment Variable | Default | Description |
|---|---|---|---|
| `server.port` | `SERVER_PORT` | `8080` | Application port |
| `app.alphavantage.api-key` | `ALPHA_VANTAGE_API_KEY` | `demo` | Alpha Vantage API key |
| `app.http.connect-timeout-ms` | - | `5000` | HTTP connect timeout (ms) |
| `app.http.read-timeout-ms` | - | `10000` | HTTP read timeout (ms) |

---

## 🗂 Project Structure

```
stock-info-service/
├── src/
│   └── main/
│       ├── java/com/example/stockinfo/
│       │   ├── StockInfoApplication.java    # Main entry point
│       │   ├── config/
│       │   │   └── AppConfig.java           # RestTemplate config
│       │   ├── controller/
│       │   │   └── StockController.java     # REST endpoints
│       │   ├── service/
│       │   │   └── StockService.java        # Business logic + API + fallback
│       │   ├── model/
│       │   │   ├── StockRequest.java        # Request DTO
│       │   │   ├── StockResponse.java       # Response DTO
│       │   │   └── ErrorResponse.java       # Error DTO
│       │   └── exception/
│       │       ├── StockNotFoundException.java
│       │       └── GlobalExceptionHandler.java
│       └── resources/
│           └── application.properties
├── Dockerfile                               # Multi-stage Docker build
├── docker-compose.yml                       # Local + cloud deployment
├── .dockerignore
├── .env                                     # Environment defaults
├── pom.xml
└── README.md
```
