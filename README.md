# MenuMaestro Main Backend

## Quickstart

Start a postgres database (e.g. via docker-compose)
Setup the application properties accordingly

```sh
cd ../
cd infrastructure
docker compose up -d
```

Start Application

```sh
mvn clean install
mvn -f application/pom.xml spring-boot:run
```

Skip Tests

```sh
mvn clean install -DskipUnitTests
mvn clean install -DskipIntegrationTests
mvn clean install -DskipTests
```

## Kubernetes Deployment with Minikube

MenuMaestro can be deployed to a local Kubernetes cluster using Minikube. The deployment includes:
- MenuMaestro application (Spring Boot + Angular)
- PostgreSQL database
- Mailhog (mock mail server)
- CloudBeaver (database management UI)

### Prerequisites

1. Install [Minikube](https://minikube.sigs.k8s.io/docs/start/)
2. Install [kubectl](https://kubernetes.io/docs/tasks/tools/)
3. Install [Docker](https://docs.docker.com/get-docker/)

### Build the Application Docker Image

First, build the application JAR:

```sh
mvn clean install
```

Start Minikube and configure your shell to use Minikube's Docker daemon:

```sh
minikube start
eval $(minikube docker-env)
```

Build the Docker image inside Minikube:

```sh
docker build -t menumaestro:latest .
```

### Deploy to Minikube

Apply all Kubernetes manifests:

```sh
kubectl apply -k k8s/
```

Check the deployment status:

```sh
kubectl get pods
kubectl get services
```

Wait for all pods to be in the `Running` state. You can watch the progress with:

```sh
kubectl get pods -w
```

### Access the Services

Get the Minikube IP:

```sh
minikube ip
```

Access the services using the NodePort mappings:

- **MenuMaestro Application**: `http://$(minikube ip):30080`
- **Mailhog UI**: `http://$(minikube ip):30025`
- **CloudBeaver**: `http://$(minikube ip):30978`

Alternatively, use Minikube's service command to open services in your browser:

```sh
minikube service menumaestro
minikube service mailhog
minikube service cloudbeaver
```

### CloudBeaver Database Configuration

On first access to CloudBeaver, you'll need to configure the PostgreSQL connection:

1. Open CloudBeaver at `http://$(minikube ip):30978`
2. Complete the initial setup wizard
3. Add a new database connection:
   - **Driver**: PostgreSQL
   - **Host**: `postgres`
   - **Port**: `5432`
   - **Database**: `dev`
   - **Username**: `db_user`
   - **Password**: `u3iGTeLr`

### Viewing Logs

View application logs:

```sh
kubectl logs -f deployment/menumaestro
kubectl logs -f deployment/postgres
kubectl logs -f deployment/mailhog
kubectl logs -f deployment/cloudbeaver
```

### Cleanup

To remove all deployed resources:

```sh
kubectl delete -k k8s/
```

To stop and delete the Minikube cluster:

```sh
minikube stop
minikube delete
```

### Troubleshooting

If pods fail to start, check the events:

```sh
kubectl describe pod <pod-name>
```

If the MenuMaestro pod fails due to image pull errors, ensure you've built the image in Minikube's Docker environment:

```sh
eval $(minikube docker-env)
docker images | grep menumaestro
```

To rebuild and redeploy:

```sh
kubectl delete -k k8s/
docker build -t menumaestro:latest .
kubectl apply -k k8s/
```