# 1. idam-health-checker

A Spring Boot application checking the health of various components of the ForgeRock system.

<!-- TOC -->

- [idam-health-checker](#idam-health-checker)
  - [CI/CD](#cicd)
  - [Interface](#interface)
  - [Statuses](#statuses)
  - [Logs](#logs)
    - [Console](#console)
    - [Application Insights](#application-insights)
  - [Secrets](#secrets)
  - [Scheduler](#scheduler)
    - [Freshness & Probe Expiry](#freshness--probe-expiry)
  - [ForgeRock AM](#forgerock-am)
    - [Is Alive Health Probe](#is-alive-health-probe)
      - [DOWN Event Triggers](#down-event-triggers)
    - [Password Grant Health Probe](#password-grant-health-probe)
      - [DOWN Event Triggers](#down-event-triggers)
  - [ForgeRock IDM](#forgerock-idm)
    - [Ping Health Probe](#ping-health-probe)
      - [DOWN Event Triggers](#down-event-triggers)
    - [Password Grant Health Probe](#password-grant-health-probe)
      - [DOWN Event Triggers](#down-event-triggers)
  - [ForgeRock DS](#forgerock-ds)
    - [LDAP Replication Health Probe](#ldap-replication-health-probe)
      - [DOWN Event Triggers](#down-event-triggers)
    - [Replication Command Probe](#replication-command-probe)
      - [DOWN Event Triggers](#down-event-triggers)
    - [Userstore Authentication Health Probe](#userstore-authentication-health-probe)
      - [DOWN Event Triggers](#down-event-triggers)
    - [TokenStore Search Health Probe](#tokenstore-search-health-probe)
      - [DOWN Event Triggers](#down-event-triggers)
    - [WIP - ForgeRock DS Backup Probe](#wip---forgerock-ds-backup-probe)
      - [DOWN Event Triggers](#down-event-triggers)

<!-- /TOC -->

## 1.1. CI/CD

To trigger Travis CI to build and publish the idam-health-checker, tag a commit in Git.

## 1.2. Interface

`http://<fqdn>:9292/admin/health`

## 1.3. Statuses

| Status  |
|---------|
| UP      |
| DOWN    |
| UNKNOWN |

## 1.4. Logs

`idam-health-checker` runs via `supervisord` on ForgeRock Virtual Machines. The `ini` file can be found at 
`/etc/supervisord.d/healthcheck.ini`. Images created by Packer and Ansible will configure the health 
checker to use the `live` Spring Profile in addition to the other system specific profiles. This 
profile uses `logback` with only the Application Insights Appender, `aiAppender`. See 
[src/main/resources/logback-spring.xml](src/main/resources/logback-spring.xml) for more.

### 1.4.1. Console 

To view console output it's advisable to set the log levels to DEBUG and the Spring Profile as `insightconsole`.

```bash
# Update /etc/supervisord.d/healthcheck.ini
sudo sed -i'.bak' \
    -e 's/WARN/DEBUG/g' \
    -e 's/,live/,insightconsole/' /etc/supervisord.d/healthcheck.ini

sudo systemctl restart supervisord

# Attach to process and parse logs
sudo strace -p$(pgrep -f supervisord) -s1000 -e write 2>&1 \
  | sed -ur 's/^.+\"(.+)\\n\".+$/\1/;s/\\n//g;s/\[?\\[0-9]{2}\[[0-9]?;?[[0-9]{2}m\]?//g'

# To restore the healthcheck.ini
sudo mv /etc/supervisord.d/healthcheck.ini /etc/supervisord.d/healthcheck.ini.debug
sudo mv /etc/supervisord.d/healthcheck.ini.bak /etc/supervisord.d/healthcheck.ini
sudo systemctl restart supervisord
```

### 1.4.2. Application Insights

Application Insights: `idam-idam-${environment}`

HealthProbe Names for customDimensions:
* AmIsAliveHealthProbe
* AmPasswordGrantHealthProbe
* FileFreshnessProbe
* ReplicationCommandProbe
* IdmPingHealthProbe
* LdapReplicationHealthProbe
* UserStoreAuthenticationHealthProbe

Example for AM isAlive Health Probe.

```
traces
| where cloud_RoleName contains "health" 
| where customDimensions contains "AmIsAliveHealthProbe" 
```

### 1.4.3. Details and Version

You can enable the details in the healthchecker with `-Dmanagement.endpoint.health.show-details="ALWAYS"`. 

Details can include information on why something is DOWN and the current healthchecker's version in its JSON output.

## 1.5. Secrets

**External Infrastructure Dependencies**

* KeyVault secrets
  * test-owner-username
  * test-owner-password
  * web-admin-client-secret
  * BINDPASSWD
  * adminUID
  * adminPassword
  * appinsights-instrumentationkey
    * [KeyVault Secret to Properties Mappings](src/main/java/uk/gov/hmcts/reform/idam/health/vault/VaultEnvironmentPostProcessor.java#L30-L38)
* Managed Identity

Secret Vault Support: [Azure KeyVault](src/main/java/uk/gov/hmcts/reform/vault)

## 1.6. Scheduler

* [ScheduledHealthProbeIndicator](src/main/java/uk/gov/hmcts/reform/idam/health/probe/ScheduledHealthProbeIndicator.java)

Status begins as `UNKNOWN`.<br>
The `refresh` function is scheduled with `checkInterval`.<br>
The initial probe is triggered.<br>
When the probe result is `true`, the status will be set to `UP`.<br>
When the probe result is `false`, the status will be set to `DOWN`.<br>
> Note: Status changes can be ignored by providing `HealthProbeFailureHandling.IGNORE` to the `ScheduledHealthProbeIndicator` initialisation. To change the status you must use `HealthProbeFailureHandling.MARK_AS_DOWN`.

### 1.6.1. Freshness & Probe Expiry

If the current probe status is `UP` or `DOWN` and current datetime is after the `statusDateTime` + `freshnessInterval` then the probe has expired.<br>
The probe is automatically expired if the Status is `UNKNOWN`.<br>
If the current probe status is UP and the probe has not expired, no action is taken.<br>

**Example Logs** 

[ScheduledHealthProbeIndicator](src/main/java/uk/gov/hmcts/reform/idam/health/probe/ScheduledHealthProbeIndicator.java#L53-L66) change of status and status ignored log messages. 

```log
2019-12-06 17:29:16,375 INFO  ProbeScheduler3 uk.gov.hmcts.reform.idam.health.probe.ScheduledHealthProbeIndicator: <PROBENAME ie. UserStoreAuthenticationHealthProbe>: Status changing from UNKNOWN to UP

2019-12-06 17:29:16,375 INFO  ProbeScheduler3 uk.gov.hmcts.reform.idam.health.probe.ScheduledHealthProbeIndicator: <PROBENAME>: Status changing from UP to DOWN

2019-12-06 17:29:16,375 INFO  ProbeScheduler3 uk.gov.hmcts.reform.idam.health.probe.ScheduledHealthProbeIndicator: <PROBENAME>: Status changing from DOWN to UP

2019-12-06 17:29:16,375 WARN  ProbeScheduler3 uk.gov.hmcts.reform.idam.health.probe.ScheduledHealthProbeIndicator: <PROBENAME>: DOWN state ignored
```

## 1.7. ForgeRock AM

A description of the health check probes for ForgeRock AM.

### 1.7.1. Is Alive Health Probe

Health check description of isAlive status for ForgeRock AM. This probe is enabled when the Spring Profile, `am`, is active.

**External Service Dependencies**

* DS Userstore
* DS Config/Tokenstore.
* IDM

**Probe Actions**

* Request `isAlive.jsp`.
* Assert the response contains `Server is ALIVE.`.

**Probe Configuration**

Spring Configuration Property: `am.root` & `am.healthprobe.isAlive`

#### 1.7.1.1. DOWN Event Triggers

* Response does not contain `Server is ALIVE.`.
* Java exception.

**Example Logs**

```log
{"LoggerName":"uk.gov.hmcts.reform.idam.health.am.AmIsAliveHealthProbe","ThreadName":"ProbeScheduler2","LoggingLevel":"ERROR","SourceType":"LOGBack","TimeStamp":"Wed, 11 Dec 2019 14:55:58 GMT","message":"AM IsAlive: response did not contain expected value"}
```

### 1.7.2. Password Grant Health Probe

Health check description of password grant probe for ForgeRock AM, which asserts that the password grant returns an access token. This probe is enabled when `am` is active however it will ignore status changes due to `HealthProbeFailureHandling.IGNORE`.

**External Service Dependencies**

* DS Userstore
* DS Config/Tokenstore.
* IDM

**External Configuration Dependencies**

* AgentProperties `name` and `secret` which can be found in Spring Properties `web.admin.client`.
* DS Config/Tokenstore.
* IDM

**Probe Actions**

* WIP 

**Probe Configuration**

Spring Configuration Property: `am.root`, `am.healthprobe.passwordGrant` & `am.healthprobe.identity`.

#### 1.7.2.1. DOWN Event Triggers

* WIP 

**Example Logs**

```log
WIP 
```

## 1.8. ForgeRock IDM

A description of the health check probes for ForgeRock IDM.

### 1.8.1. Ping Health Probe

Health check description of annonymous ping status for ForgeRock IDM. This probe is enabled when the Spring Profile, `am`, is active.

**External Service Dependencies**

* DS Userstore
* DS Config/Tokenstore.
* IDM

**Probe Actions**

* Request `isAlive.jsp`.
* Assert the response contains `Server is ALIVE.`.

**Probe Configuration**

Spring Configuration Property: `am.root` & `am.healthprobe.isAlive`

#### 1.8.1.1. DOWN Event Triggers

* Response does not contain `Server is ALIVE.`.
* Java exception.

**Example Logs**

```log
{"LoggerName":"uk.gov.hmcts.reform.idam.health.am.AmIsAliveHealthProbe","ThreadName":"ProbeScheduler2","LoggingLevel":"ERROR","SourceType":"LOGBack","TimeStamp":"Wed, 11 Dec 2019 14:55:58 GMT","message":"AM IsAlive: response did not contain expected value"}
```

### 1.8.2. Password Grant Health Probe

Health check description of password grant probe for ForgeRock AM, which asserts that the password grant returns an access token. This probe is enabled when `am` is active however it will ignore status changes due to `HealthProbeFailureHandling.IGNORE`.

**External Service Dependencies**

* DS Userstore
* DS Config/Tokenstore.
* IDM

**External Configuration Dependencies**

* AgentProperties `name` and `secret` which can be found in Spring Properties `web.admin.client`.
* DS Config/Tokenstore.
* IDM

**Probe Actions**

* WIP 

**Probe Configuration**

Spring Configuration Property: `am.root`, `am.healthprobe.passwordGrant` & `am.healthprobe.identity`.

#### 1.8.2.1. DOWN Event Triggers

* WIP 

**Example Logs**

```log
WIP 
```

## 1.9. ForgeRock DS

A description of the health check probes for ForgeRock DS.

### 1.9.1. LDAP Replication Health Probe

Health check description for LDAP replication using LDAP attributes query for ForgeRock DS. This probe is enabled when `userstore` or `tokenstore` is active and `single` is inactive.

**External Config Dependencies**

* `cn=Directory Manager`
* `cn=Replication,cn=monitor`
* KeyVault secrets
  * BINDPASSWD

**Probe Actions**

* [Query LDAP Replication Monitor for List of Attributes](src/main/java/uk/gov/hmcts/reform/idam/health/ldap/LdapReplicationHealthProbe.java#L61-L72)
* [Parse and Categorise LDAP Responses into Types](src/main/java/uk/gov/hmcts/reform/idam/health/ldap/LdapReplicationHealthProbe.java#L191-L270)
* [Check Typed Responses for Replay Errors](src/main/java/uk/gov/hmcts/reform/idam/health/ldap/LdapReplicationHealthProbe.java#L131-L135)
* [Check Typed Responses for Missing Changes](src/main/java/uk/gov/hmcts/reform/idam/health/ldap/LdapReplicationHealthProbe.java#L137-L140)
* [Check Typed Responses for Delays](src/main/java/uk/gov/hmcts/reform/idam/health/ldap/LdapReplicationHealthProbe.java#L142-L144)

**Probe Configuration**

Spring Configuration Property: `ldap`

**Attributes**

| LDAP Attribute    | Log Mapping | Description |
|-------------------|-------------|-------------|
| status            | status      |             |
| pending-updates   | pending     |             |
| missing-changes   | missing     |             |
| approximate-delay | delay       |             |
| sent-updates      | sent        |             |
| recieved-updates  | recieved    |             |
| replayed-updates  | replayed    |             |

**Record Types**

| Record Type            | Description                                                                                                |
|------------------------|------------------------------------------------------------------------------------------------------------|
| LOCAL_DS               | Record describes the local Directory Server                                                                |
| LOCAL_RS               | Record describes the local Replication Server                                                              |
| LOCAL_RS_CONN_DS       | Record describes the local Replication Server and connected Directory Server                               |
| REMOTE_CONN_RS         | Record describes the local Replication Server and connected Replication Server                             |
| REMOTE_CONN_RS_CONN_DS | Record describes the local Replication Server, connected Replication Server and connected Directory Server |
| UNKNOWN                | Record does not match any of the above                                                                     |

#### 1.9.1.1. DOWN Event Triggers

* [Failed replay check](src/main/java/uk/gov/hmcts/reform/idam/health/ldap/LdapReplicationHealthProbe.java#L131-L135)
  * If the `received-updates` minus the `replayed-updates` is less than or equal to the missing updates threshold. 
* [Missing changes](src/main/java/uk/gov/hmcts/reform/idam/health/ldap/LdapReplicationHealthProbe.java#L142-L144)
  * If the `missing-changes` are greater than the missing updates threshold. 
* [Failed delay check](src/main/java/uk/gov/hmcts/reform/idam/health/ldap/LdapReplicationHealthProbe.java#L137-L139)
  * If the `approximate-delay` is less than or equal to the approximate delay threshold. 

**Example Logs**

[ReplicationCommandProbe](src/main/java/uk/gov/hmcts/reform/idam/health/command/ReplicationCommandProbe.java)

```log
2019-12-09 14:08:26,443 INFO  ProbeScheduler3 uk.gov.hmcts.reform.idam.health.command.ReplicationCommandProbe: Configuring with command /opt/opendj/bin/dsreplication status -X --adminUID %s --adminPassword %s --port 4444 -s -n and password value from properties
```

[LOCAL_DS](src/main/java/uk/gov/hmcts/reform/idam/health/ldap/LdapReplicationHealthProbe.java)

```log
2019-12-09 14:08:26,539 INFO  ProbeScheduler2 uk.gov.hmcts.reform.idam.health.ldap.LdapReplicationHealthProbe: LDAP Replication: LOCAL_DS okay, ds:forgerock-ds-userstore.service.core-compute-idam-perftest.internal:50096,status:Normal,pending:0,missing:-1,sent:31206,received:30015,replayed:30015,delay:-1
```

[LOCAL_RS](src/main/java/uk/gov/hmcts/reform/idam/health/ldap/LdapReplicationHealthProbe.java)

```log
2019-12-09 14:08:26,540 INFO  ProbeScheduler2 uk.gov.hmcts.reform.idam.health.ldap.LdapReplicationHealthProbe: LDAP Replication: LOCAL_RS okay, rs:forgerock-ds-userstore-idam-perftest000002.service.core-compute-idam-perftest.internal:8989,status:null,pending:-1,missing:4696949,sent:-1,received:-1,replayed:-1,delay:-1
```

[LOCAL_RS_CONN_DS](src/main/java/uk/gov/hmcts/reform/idam/health/ldap/LdapReplicationHealthProbe.java)

```log
2019-12-09 14:08:26,541 INFO  ProbeScheduler2 uk.gov.hmcts.reform.idam.health.ldap.LdapReplicationHealthProbe: LDAP Replication: LOCAL_RS_CONN_DS okay, rs:forgerock-ds-userstore-idam-perftest000002.service.core-compute-idam-perftest.internal:8989,connected-ds:forgerock-ds-userstore.service.core-compute-idam-perftest.internal:50096,status:null,pending:-1,missing:0,sent:30015,received:31206,replayed:-1,delay:0
```

[REMOTE_CONN_RS](src/main/java/uk/gov/hmcts/reform/idam/health/ldap/LdapReplicationHealthProbe.java)

```log
2019-12-09 14:08:26,542 INFO  ProbeScheduler2 uk.gov.hmcts.reform.idam.health.ldap.LdapReplicationHealthProbe: LDAP Replication: REMOTE_CONN_RS okay, rs:forgerock-ds-userstore-idam-perftest000002.service.core-compute-idam-perftest.internal:8989,connected-rs:forgerock-ds-userstore-idam-perftest000001.service.core-compute-idam-perftest.internal:8989,status:null,pending:-1,missing:4696949,sent:31205,received:30012,replayed:-1,delay:-1
```

[REMOTE_CONN_RS_CONN_DS](src/main/java/uk/gov/hmcts/reform/idam/health/ldap/LdapReplicationHealthProbe.java)

```log
2019-12-09 14:08:26,544 INFO  ProbeScheduler2 uk.gov.hmcts.reform.idam.health.ldap.LdapReplicationHealthProbe: LDAP Replication: REMOTE_CONN_RS_CONN_DS okay, rs:forgerock-ds-userstore-idam-perftest000002.service.core-compute-idam-perftest.internal:8989,connected-rs:forgerock-ds-userstore-idam-perftest000001.service.core-compute-idam-perftest.internal:8989,connected-ds:forgerock-ds-userstore.service.core-compute-idam-perftest.internal:33920,status:null,pending:-1,missing:0,sent:-1,received:-1,replayed:-1,delay:0
```

### 1.9.2. Replication Command Probe

Health check description for ForgeRock DS replication through the `dsreplication` command line tool. This probe is enabled when `userstore`, `tokenstore` or `replication` is active and `single` is inactive.

**External Config Dependencies**

* KeyVault secrets
  * adminUID
  * adminPassword

**ForgeRock 5.5 Example Command Response**

```bash
Suffix DN                 : Server                                          : Entries : Replication enabled : DS ID : RS ID : RS Port (1) : M.C. (2) : A.O.M.C. (3) : Security (4)
--------------------------:-------------------------------------------------:---------:---------------------:-------:-------:-------------:----------:--------------:-------------
dc=reform,dc=hmcts,dc=net : forgerock-ds-userstore-idam-perftest000001:4444 : 498270  : true                : 27069 : 13539 : 8989        : 0        :              : true
dc=reform,dc=hmcts,dc=net : forgerock-ds-userstore-idam-perftest000002:4444 : 498270  : true                : 10519 : 5416  : 8989        : 0        :              : true
dc=reform,dc=hmcts,dc=net : forgerock-ds-userstore-idam-perftest000004:4444 : 498270  : true                : 15309 : 16214 : 8989        : 0        :              : true

[1] The port used to communicate between the servers whose contents are being
replicated.
[2] The number of changes that are still missing on this server (and that have
been applied to at least one of the other servers).
[3] Age of oldest missing change: the date on which the oldest change that has
not arrived on this server was generated.
[4] Whether the replication communication through the replication port is
encrypted or not.
```

**ForgeRock 6.5 Example Command Response**

```bash
Suffix DN                 : Server                                                                               : Entries : Replication enabled : DS ID : RS ID : RS Port (1) : Delay (ms) : Security (2)
--------------------------:--------------------------------------------------------------------------------------:---------:---------------------:-------:-------:-------------:------------:-------------
dc=reform,dc=hmcts,dc=net : forgerock-ds-tokenstore-idam-saat000002.service.core-compute-idam-saat.internal:4444 : 827     : true                : 2615  : 21251 : 8989        : 0          : true
dc=reform,dc=hmcts,dc=net : forgerock-ds-tokenstore-idam-saat000004.service.core-compute-idam-saat.internal:4444 : 827     : true                : 10324 : 21828 : 8989        : 0          : true
dc=reform,dc=hmcts,dc=net : forgerock-ds-tokenstore-idam-saat000005.service.core-compute-idam-saat.internal:4444 : 827     : true                : 3056  : 6463  : 8989        : 0          : true

[1] The port used to communicate between the servers whose contents are being
replicated.
[2] Whether the replication communication through the replication port is
encrypted or not.
```

**Probe Actions**

* [Get the replication status by running the specified command.](src/main/java/uk/gov/hmcts/reform/idam/health/command/ReplicationCommandProbe.java#L40)
* [Identify the current host and other replication servers in the replication information.](src/main/java/uk/gov/hmcts/reform/idam/health/command/ReplicationCommandProbe.java#L44-L56)
* [Verify host replication by checking the missing changes are not greater than the missing updates threshold.](src/main/java/uk/gov/hmcts/reform/idam/health/command/ReplicationCommandProbe.java#L86-L93)
* [Find the max entries count from the command output stream. Compare this to the current number of entries on the current host.](src/main/java/uk/gov/hmcts/reform/idam/health/command/ReplicationCommandProbe.java#L95-L106)
* [Handle command execution errors & Java exceptions.](src/main/java/uk/gov/hmcts/reform/idam/health/command/ReplicationCommandProbe.java#L58-L70)

**Probe Configuration**

Spring Configuration Property Object: `replication.healthprobe`

**Setting the Missing Updates Threshold**

[replication.healthprobe.command.missing-updates-threshold](src/main/resources/application.yaml#L110)

#### 1.9.2.1. DOWN Event Triggers

* [Missing Changes are Greater than the Threshold](src/main/java/uk/gov/hmcts/reform/idam/health/command/ReplicationCommandProbe.java#L86-L93)
* [Compare Entries between Replication Servers](src/main/java/uk/gov/hmcts/reform/idam/health/command/ReplicationCommandProbe.java#L95-L106)
  * If the host entries count is less than the max or less than the (max entries - `entryDifferenceThreshold`)
* [Replication Command Errors](src/main/java/uk/gov/hmcts/reform/idam/health/command/ReplicationCommandProbe.java#L58-L66)
* [Java Exception](src/main/java/uk/gov/hmcts/reform/idam/health/command/ReplicationCommandProbe.java#L68-L70)

**Example Logs**

[Host Replication Info](src/main/java/uk/gov/hmcts/reform/idam/health/command/ReplicationCommandProbe.java)

```log
2019-12-09 14:08:29,677 INFO  ProbeScheduler3 uk.gov.hmcts.reform.idam.health.command.ReplicationCommandProbe: ReplicationCommand: Host replication info: ReplicationInfo(suffix=dc=reform,dc=hmcts,dc=net, hostName=forgerock-ds-userstore-idam-perftest000002.service.core-compute-idam-perftest.internal:4444, entries=498269, replicationEnabled=true, dsID=10519, rsId=5416, rsPort=8989, missingChanges=0, ageOfMissingChanges=null, securityEnabled=true)
```

[Replicated Host ReplicationInfo](src/main/java/uk/gov/hmcts/reform/idam/health/command/ReplicationCommandProbe.java)

```log
2019-12-09 14:08:29,679 INFO  ProbeScheduler3 uk.gov.hmcts.reform.idam.health.command.ReplicationCommandProbe: ReplicationCommand: Replicated host: ReplicationInfo(suffix=dc=reform,dc=hmcts,dc=net, hostName=forgerock-ds-userstore-idam-perftest000001.service.core-compute-idam-perftest.internal:4444, entries=498266, replicationEnabled=true, dsID=27069, rsId=13539, rsPort=8989, missingChanges=0, ageOfMissingChanges=null, securityEnabled=true)
```

### 1.9.3. Userstore Authentication Health Probe

Health check description for ForgeRock DS User Store using LDAP queries. This probe is enabled when `userstore` is active.

**External Config Dependencies**

* `cn=Directory Manager`
* KeyVault
  * test-owner-username
  * test-owner-password

**Probe Actions**

* [Search LDAP for test user](src/main/java/uk/gov/hmcts/reform/idam/health/userstore/UserStoreAuthenticationHealthProbe.java#L42-L46)
* [Authenicate as LDAP test user](src/main/java/uk/gov/hmcts/reform/idam/health/userstore/UserStoreAuthenticationHealthProbe.java#L50-L53)

**Probe Configuration**

Spring Property Configuration Object: `userstore.healthprobe`

| Spring Profile | ScheduledHealthProbeIndicator Configuration                                                                                                       |
|----------------|---------------------------------------------------------------------------------------------------------------------------------------------------|
| single         | [HealthProbeFailureHandling.IGNORE](src/main/java/uk/gov/hmcts/reform/idam/health/userstore/UserStoreHealthProbeConfiguration.java#L27-L28)       |
| userstore      | [HealthProbeFailureHandling.MARK_AS_DOWN](src/main/java/uk/gov/hmcts/reform/idam/health/userstore/UserStoreHealthProbeConfiguration.java#L35-L44) |

#### 1.9.3.1. DOWN Event Triggers

* Test User Does Not Exist
  * Confirm KeyVault secret `test-owner-username`.
* Failed Authentication with Test User
  * Confirm KeyVault secret `test-owner-password`.
  * Update the password from IDM if required.
    > Note: You may need to set the ds-userstore healthcheck profile to `optimist,live` to make the LDAP MODIFY successful.
* Exceptions
  * Connection.
  * etc.

**Example Logs**

[UserStoreAuthenticationHealthProbe](src/main/java/uk/gov/hmcts/reform/idam/health/userstore/UserStoreAuthenticationHealthProbe.java#L55-L56) was successful.

```log
2019-12-09 11:30:25,926 INFO  ProbeScheduler3 uk.gov.hmcts.reform.idam.health.userstore.UserStoreAuthenticationHealthProbe: UserStore Auth: success
```

[UserStoreAuthenticationHealthProbe](src/main/java/uk/gov/hmcts/reform/idam/health/userstore/UserStoreAuthenticationHealthProbe.java#L47-L48) LDAP query for `test-owner-username` returned empty. The user does not exist.

```log
2019-12-09 11:30:25,926 WARN  ProbeScheduler3 uk.gov.hmcts.reform.idam.health.userstore.UserStoreAuthenticationHealthProbe: UserStore Auth: test user does not exist
```

[UserStoreAuthenticationHealthProbe](src/main/java/uk/gov/hmcts/reform/idam/health/userstore/UserStoreAuthenticationHealthProbe.java#L58-L59) LDAP authentication failed for credentials `test-owner-username` & `test-owner-password`.

```log
2019-12-06 17:29:16,375 ERROR ProbeScheduler3 uk.gov.hmcts.reform.idam.health.userstore.UserStoreAuthenticationHealthProbe: UserStore Auth: authentication failed for filter (uid=idam@test.localhost)
```

[UserStoreAuthenticationHealthProbe](src/main/java/uk/gov/hmcts/reform/idam/health/userstore/UserStoreAuthenticationHealthProbe.java#L61-L62) encountered a communication exception. Connection to LDAPS was refused.

```log
2019-12-06 17:29:12,854 ERROR ProbeScheduler3 uk.gov.hmcts.reform.idam.health.userstore.UserStoreAuthenticationHealthProbe: UserStore Auth: forgerock-ds-userstore.service.core-compute-idam-saat.internal:1639; nested exception is javax.naming.CommunicationException: forgerock-ds-userstore.service.core-compute-idam-saat.internal:1639 [Root exception is java.net.ConnectException: Connection refused (Connection refused)] [CommunicationException]
```

### 1.9.4. TokenStore Search Health Probe

Health check description for ForgeRock DS Token Store using LDAP queries. This probe is enabled when `tokenstore` is active.

**External Config Dependencies**

* `cn=schema providers,cn=config`
* KeyVault
  * test-owner-username
  * test-owner-password

**Probe Actions**

* Query LDAP for any object in `cn=schema providers,cn=config`.

**Probe Configuration**

Spring Property Configuration Object: `tokenstore.healthprobe`

| Spring Profile | ScheduledHealthProbeIndicator Configuration                                                                                                         |
|----------------|-----------------------------------------------------------------------------------------------------------------------------------------------------|
| single         | [HealthProbeFailureHandling.IGNORE](ssrc/main/java/uk/gov/hmcts/reform/idam/health/tokenstore/TokenStoreHealthProbeConfiguration.java#L27-L28)      |
| tokenstore     | [HealthProbeFailureHandling.MARK_AS_DOWN](src/main/java/uk/gov/hmcts/reform/idam/health/tokenstore/TokenStoreHealthProbeConfiguration.java#L35-L44) |

#### 1.9.4.1. DOWN Event Triggers

* Empty Response for LDAP Search
* Exception

**Example Logs**

```log
2019-12-09 10:30:04,541 INFO  ProbeScheduler3 uk.gov.hmcts.reform.idam.health.tokenstore.TokenStoreSearchHealthProbe: TokenStore Search: success

2019-12-09 10:30:04,541 ERROR  ProbeScheduler3 uk.gov.hmcts.reform.idam.health.tokenstore.TokenStoreSearchHealthProbe: TokenStore Search: response is empty
```

### 1.9.5. WIP - ForgeRock DS Backup Probe

Health check description for ForgeRock DS backups. This probe is enabled when `backup` is active.

**External Config Dependencies**

*

**Probe Actions**

*

**Probe Configuration**


#### 1.9.5.1. DOWN Event Triggers


**Example Logs**

```log

```