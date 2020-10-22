# AppService
Application Framework

# Build the applicaiton jar

```
mvn install
```

# Building and running docker image

```
docker build -t app-service-image .

docker run -p8080:8080 -p8081:8081 -it --rm --name app-service app-service-image 
```



