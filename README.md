# idam-health-checker
Health checker for various components of the ForgeRock system

If all checks are working, hitting http://localhost:9292/health should return something like

```json
{"status":"UP","accessToken":{"status":"UP","message":"Server returned access_token"},"am":{"status":"UP","message":"Server is ALIVE"},"diskSpace":{"status":"UP","total":250685575168,"free":158809206784,"threshold":10485760}}
```
