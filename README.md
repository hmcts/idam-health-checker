# healthchecker

Application to check the health of ForgeRock AM running on the same server.

If all AM checks are working, hitting http://localhost:9292/health shoudl return something like


```json
{"status":"UP","accessToken":{"status":"UP","message":"Server returned access_token"},"am":{"status":"UP","message":"Server is ALIVE"},"diskSpace":{"status":"UP","total":250685575168,"free":158809206784,"threshold":10485760}}
```
