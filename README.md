# Akka - Heimdall
Base project whose goal is to integrate akka http and heimdall plugin

## Build Docker

```
mvn clean install package docker:build
```

## Push docker

Default registry: 10.48.238.129:5000  (R3)

```
mvn clean install package docker:build docker:push -Ddocker.registry=<IP:PORT>
```