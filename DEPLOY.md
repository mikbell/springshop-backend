# Deploy Docker su VPS

Questa configurazione avvia SpringShop con PostgreSQL e Redis in container Docker. L'API resta esposta solo su `127.0.0.1:3000` per essere pubblicata da un reverse proxy come Nginx o Caddy.

## Prerequisiti

- VPS Linux con Docker Engine e Docker Compose plugin.
- Dominio puntato alla VPS.
- Reverse proxy con HTTPS davanti a `http://127.0.0.1:3000`.

## Prima configurazione

1. Copia il progetto sulla VPS.
2. Crea il file `.env`:

   ```bash
   cp .env.example .env
   ```

3. Modifica `.env` e sostituisci tutti i placeholder:
   - `DB_PASSWORD`: password lunga e casuale.
   - `JWT_SECRET_KEY`: secret Base64 generato con `openssl rand -base64 32`.
   - `CORS_ALLOWED_ORIGINS`: origin reali del frontend, separati da virgola.
   - `STRIPE_*`: chiavi e URL dell'ambiente Stripe corretto.
   - `SPRINGDOC_*`: lascia `false` in produzione, salvo necessità di pubblicare Swagger/OpenAPI.

## Avvio

```bash
docker compose -f docker-compose.prod.yml up -d --build
```

Verifica lo stato:

```bash
docker compose -f docker-compose.prod.yml ps
docker compose -f docker-compose.prod.yml logs -f api
curl http://127.0.0.1:3000/actuator/health
```

## Aggiornamento

```bash
git pull
docker compose -f docker-compose.prod.yml up -d --build
docker image prune -f
```

## Reverse proxy

Esempio Nginx:

```nginx
server {
    listen 80;
    server_name api.example.com;

    location / {
        proxy_pass http://127.0.0.1:3000;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

Abilita HTTPS con Certbot o con il sistema TLS del reverse proxy scelto.

## Backup

Backup database:

```bash
docker compose -f docker-compose.prod.yml exec -T postgres pg_dump -U "$DB_USERNAME" "$DB_NAME" > springshop.sql
```

Le immagini prodotto sono nel volume Docker `product_images`. Per un backup completo includi anche i volumi `postgres_data`, `redis_data` e `product_images`.
