# Consumer-Driven Contracts

This service is meant to be consumed by other backends, so its HTTP surface is described as
**Spring Cloud Contract** definitions under `src/test/resources/contracts/`. From these, the
provider side generates verification tests and publishes **stubs** that consumers run their own
integration tests against — breaking changes to the API then fail a build instead of production.

## Status

The verifier plugin is **not wired into the build yet**: Spring Cloud has no release train that
targets Spring Boot 4.0.x at time of writing, and adding it would break dependency resolution.
The contracts (`*.yml`) are kept as the source of truth and a ready-to-enable scaffold.

## Enabling it (once a compatible Spring Cloud release exists)

1. Import the Spring Cloud BOM in `pom.xml` `dependencyManagement` and add:
   ```xml
   <dependency>
     <groupId>org.springframework.cloud</groupId>
     <artifactId>spring-cloud-starter-contract-verifier</artifactId>
     <scope>test</scope>
   </dependency>
   ```
2. Add the plugin and point it at a base test class that boots the app (reuse
   `CallApiIT`'s setup) so generated tests have a running context:
   ```xml
   <plugin>
     <groupId>org.springframework.cloud</groupId>
     <artifactId>spring-cloud-contract-maven-plugin</artifactId>
     <extensions>true</extensions>
     <configuration>
       <baseClassForTests>com.callkeypoints.backend.ContractVerifierBase</baseClassForTests>
     </configuration>
   </plugin>
   ```
3. `./mvnw verify` now generates + runs provider tests from the contracts and installs the stub
   jar (`ckp-<version>-stubs.jar`) for consumers to use with `@AutoConfigureStubRunner`.

## Adding a contract

Drop a new `*.yml` (or Groovy) file in `src/test/resources/contracts/`. Each describes one
request/response pair. Keep them deterministic (e.g. auth/validation failures, fixed shapes).
