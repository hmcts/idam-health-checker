# idam-health-checker

A Spring Boot application checking the health of various components of the ForgeRock system.

Tested components include `AM` and `IDM` components.

If all checks are working, hitting `http://localhost:9292/health` should return something like

```json
{"status":"UP","accessToken":{"status":"UP","message":"Server returned access_token"},"am":{"status":"UP","message":"Server is ALIVE"},"diskSpace":{"status":"UP","total":250685575168,"free":158809206784,"threshold":10485760}}
```

There are two main modes of operation, `dev` and `live`. These can be set within `application.properties` by setting the
property e.g.:

```spring.profiles.active = dev```

Setting this to `live` will mean that a real Azure Vault is attempted to be reached to extract sectret values. If the active profile
is `dev` then values will be loaded from a properties file.

Individual `HealthIndicator` classes are additionally enabled here by adding different profiles 
to `spring.profiles.active` e.g. 

```spring.profiles.active = dev,am,idm```

which will enable both the AM and IDM checks. If only one of `dev` or `live` is defined, then the (default)
file space check will be the only `HeathIndicator` contributing to the overall status.

```vault.base.url
vault.client.id
vault.client.key
```

will be passed in as environment variables as the jar file is run.
