# Kubernetes - Sistema de Gestión de Inventario

## Estructura de archivos

```
k8s/
├── 00-namespace.yml          # Namespace: inventory-system
├── 01-secrets.yml            # Credenciales (MySQL, RabbitMQ, JWT, AWS)
├── 02-configmap.yml          # Variables de configuración no sensibles
├── 03-mysql.yml              # MySQL Deployment + Service + PVC
├── 04-redis.yml              # Redis Deployment + Service + PVC
├── 05-rabbitmq.yml           # RabbitMQ Deployment + Service + PVC
├── 06-msvc-auth.yml          # Auth microservice Deployment + Service (incluye S3)
├── 08-msvc-inventory.yml     # Inventory microservice Deployment + Service
├── 09-msvc-notifications.yml # Notifications microservice Deployment + Service
└── 10-msvc-gateway.yml       # Gateway Deployment + Service (LoadBalancer)
```

## Antes de desplegar

### 1. Construir las imágenes Docker

Desde la raíz del proyecto, construye cada microservicio:

```bash
docker build -t msvc-auth:latest ./microservice-auth
docker build -t msvc-inventory:latest ./microservice-inventory
docker build -t msvc-notifications:latest ./microservice-notifications
docker build -t msvc-gateway:latest ./microservice-gateway
```

Si usas un registry (Docker Hub, ECR, GCR, etc.), tagea y sube las imágenes:

```bash
docker tag msvc-auth:latest tu-registry/msvc-auth:latest
docker push tu-registry/msvc-auth:latest
# Repite para cada microservicio
```

Luego actualiza el campo `image:` en cada Deployment YAML con la ruta completa.

### 2. Actualizar los Secrets

Edita `01-secrets.yml` con tus credenciales reales codificadas en base64:

```bash
# Ejemplo para generar un valor base64
echo -n "tu_password" | base64
```

**Especialmente importante:** actualiza `aws-secret` con tus claves reales de AWS.

## Despliegue

### Opción A: Aplicar en orden (recomendado)

```bash
kubectl apply -f k8s/00-namespace.yml
kubectl apply -f k8s/01-secrets.yml
kubectl apply -f k8s/02-configmap.yml
kubectl apply -f k8s/03-mysql.yml
kubectl apply -f k8s/04-redis.yml
kubectl apply -f k8s/05-rabbitmq.yml

# Esperar a que la infraestructura esté lista
kubectl wait --for=condition=ready pod -l app=mysql -n inventory-system --timeout=120s
kubectl wait --for=condition=ready pod -l app=redis -n inventory-system --timeout=60s
kubectl wait --for=condition=ready pod -l app=rabbitmq -n inventory-system --timeout=90s

# Desplegar microservicios
kubectl apply -f k8s/06-msvc-auth.yml
kubectl apply -f k8s/08-msvc-inventory.yml
kubectl apply -f k8s/09-msvc-notifications.yml
kubectl apply -f k8s/10-msvc-gateway.yml
```

### Opción B: Aplicar todo de una vez

```bash
kubectl apply -f k8s/
```

## Verificar el despliegue

```bash
# Ver todos los pods
kubectl get pods -n inventory-system

# Ver todos los services
kubectl get services -n inventory-system

# Ver la IP externa del gateway
kubectl get service msvc-gateway-external -n inventory-system

# Ver logs de un microservicio
kubectl logs -l app=msvc-auth -n inventory-system --tail=100

# Describir un pod con problemas
kubectl describe pod <nombre-del-pod> -n inventory-system
```

## DNS interno de Kubernetes

Los microservicios se comunican entre sí usando los nombres de los Services:

| Servicio       | DNS interno              | Puerto |
|----------------|--------------------------|--------|
| MySQL          | mysql-service            | 3306   |
| Redis          | redis-service            | 6379   |
| RabbitMQ       | rabbitmq-service         | 5672   |
| Auth           | msvc-auth-service        | 8081   |
| Inventory      | msvc-inventory-service   | 8083   |
| Notifications  | msvc-notifications-service | 8084 |
| Gateway        | msvc-gateway-service     | 8080   |

## Eliminar todo

```bash
kubectl delete namespace inventory-system
```
