
## Test
```bash
docker-compose run --rm --entrypoint "mvn clean test" keycloak-spi-builder 
```

## Package
```bash
docker-compose run --rm keycloak-spi-builder
```

## Run keycloak
```bash
docker-compose down 
docker-compose run --rm keycloak-spi-builder 
docker-compose up keycloak
```
* Go to http://localhost:8080

## Integrated tests
* Exec
```bash
docker-compose -f integrated-tests/docker-compose.yaml run --rm --entrypoint "bash 1-config-client.sh" keycloak-script-runner
docker-compose -f integrated-tests/docker-compose.yaml run --rm --entrypoint "bash 2-token.sh" keycloak-script-runner
```
