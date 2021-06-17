# quarkus-reactive-distributed-cache-layer project

This project uses Quarkus, the Supersonic Subatomic Java Framework.

This project demonstrates a way to use custom interceptors as a means of handling with
distributed caches as a cache layer in the application.

[Distributed Cache Layering with Infinispan and Quarkus](https://dvddhln.medium.com/distributed-cache-layering-with-infinispan-and-quarkus-d8df4188efd4)


## Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```shell script
./mvnw compile quarkus:dev
```


### Running
    docker run -p 11222:11222 -e USER="Titus Bramble" -e PASS="Shambles"   quay.io/infinispan/server:12.1.4.Final

### Get Request
    http://localhost:8080/v1/services/1

### Checking the infinispan UI
    http://localhost:11222/console/cache/my-cache
    
### Credentials
    
    username: Titus Bramble
    password: "Shambles"

### Quarkus Configuration
    quarkus:
      infinispan-client:
        auth-username: "Titus Bramble"
        auth-password: "Shambles"
        auth-realm: default
        auth-server-name: infinispan
        sasl-mechanism: DIGEST-MD5
    cache:
      <infinispan>
        <cache-container>
          <replicated-cache name="<name-override>">
            <encoding>
              <key media-type="application/x-java-object"/>
              <value media-type="application/x-java-object"/>
            </encoding>
          </replicated-cache>
        </cache-container>
      </infinispan>