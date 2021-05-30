# Wildfly Bootable Jar connecting to Oracle Database

This repo contains a working example of a Wildfly Bootable Jar that connects to an Oracle Database;

Wildfly Bootable Jar is an alternative to Spring Boot which uses Wildfly as the embedded server;

Wildfly Bootable Jar allows you to trim the server and keep just the pieces you are interested in: as we know Wildfly 
comes with a ton of features like clustering, messaging, EJBs, JBatch, Microprofile etc... and with Bootable jar you can
keep just the pieces you need (the pieces are called layers - you can see the list of all available layers here
https://docs.wildfly.org/23/Bootable_Guide.html#wildfly_layers);

## Oracle DB

First you need an Oracle database: I used the containerized version of Oracle-XE version 11
[oracleinanutshell/oracle-xe-11g](https://hub.docker.com/r/oracleinanutshell/oracle-xe-11g);

You can start it using Podman:

```shell
podman pull oracleinanutshell/oracle-xe-11g:latest
podman run -d -p 49161:1521 oracleinanutshell/oracle-xe-11g
```

or Docker:

```shell
docker pull oracleinanutshell/oracle-xe-11g:latest
docker run -d -p 49161:1521 oracleinanutshell/oracle-xe-11g
```

This way you have a running Oracle DB on your laptop; you can connect to it using the following info:

```shell
URL: jdbc:oracle:thin:@localhost:49161:xe
USERNAME: system
PASSWORD: oracle
```

## Oracle Layers

Wildfly Bootable Jar needs a couple of layers that provide:
- the Oracle JDBC driver 
- the Wildfly Database connection

You can obtain these layers by cloning and building the following repository:

```shell
git clone https://github.com/wildfly-extras/wildfly-datasources-galleon-pack.git
cd wildfly-datasources-galleon-pack
mvn install -DskipTests -Denforcer.skip=true
```

The repository actually provides layers for all most common databases (PostgreSQL, MySQL, etc...);


## Wildfly Bootable Jar

After you have a working Oracle Database and the layers to connect to it, you can create the Wildfly Bootable Jar; 

You can just clone this repository and build it:

```shell
git clone https://github.com/Tommy74/wildfly-bootable-jar-oracle.git
cd wildfly-bootable-jar-oracle
mvn package -DskipTests -Denforcer.skip=true
```

Now we can set the environment variables that tell the Bootable Jar how-to connect to the Oracle Database and start the
Bootable Jar:

```shell
export ORACLE_PASSWORD=oracle
export ORACLE_URL=jdbc:oracle:thin:@localhost:49161:xe
export ORACLE_USER=system
export ORACLE_DATASOURCE=OracleDS
java -jar target/wildfly-bootable-jar-oracle-bootable.jar
```

Invoke the following URL and see that the response tells you the Oracle schema you are connected to:

```shell
curl http://localhost:8080/api/datasource
Hello from WildFly bootable jar - Oracle schema SYSTEM!
```

if you prefer using a simple servlet rather that a JAX-RS endpoint:

```shell
curl http://localhost:8080/datasource
schema=SYSTEM
```

## Wildfly Bootable Jar Datasource

This paragraph gives a little explanation about what happens under the hood;

If you look into the `pom.xml` file you can see the two layers that allows us to connect to Oracle:

```xml
<layer>oracle-driver</layer>
<layer>oracle-datasource</layer>
```

The `oracle-driver` provides the Oracle JDBC driver to the Wildfly Bootable Jar;

The `oracle-datasource` provides a parametric connection to the Oracle Database to the Wildfly Bootable Jar; if you look
inside the `/standalone/configuration/standalone.xml` file inside the file `wildfly.zip` which is inside 
`target/wildfly-bootable-jar-oracle-bootable.jar`, you find the following:

```xml
        <subsystem xmlns="urn:jboss:domain:datasources:6.0">
            <datasources>
                <datasource jndi-name="java:jboss/datasources/${env.ORACLE_DATASOURCE,env.OPENSHIFT_ORACLE_DATASOURCE:OracleDS}" pool-name="OracleDS" enabled="true" use-java-context="true" use-ccm="true" statistics-enabled="${wildfly.datasources.statistics-enabled:${wildfly.statistics-enabled:false}}">
                    <connection-url>${env.ORACLE_URL, env.OPENSHIFT_ORACLE_URL}</connection-url>
                    <driver>oracle</driver>
                    <security>
                        <user-name>${env.ORACLE_USER, env.OPENSHIFT_ORACLE_DB_USERNAME}</user-name>
                        <password>${env.ORACLE_PASSWORD, env.OPENSHIFT_ORACLE_DB_PASSWORD}</password>
                    </security>
                    <validation>
                        <valid-connection-checker class-name="org.jboss.jca.adapters.jdbc.extensions.oracle.OracleValidConnectionChecker"/>
                        <validate-on-match>true</validate-on-match>
                        <background-validation>false</background-validation>
                        <stale-connection-checker class-name="org.jboss.jca.adapters.jdbc.extensions.oracle.OracleStaleConnectionChecker"/>
                        <exception-sorter class-name="org.jboss.jca.adapters.jdbc.extensions.oracle.OracleExceptionSorter"/>
                    </validation>
                </datasource>
                <drivers>
                    <driver name="oracle" module="com.oracle.ojdbc">
                        <xa-datasource-class>oracle.jdbc.xa.client.OracleXADataSource</xa-datasource-class>
                    </driver>
                </drivers>
            </datasources>
        </subsystem>
```

this is exactly a connection to the Oracle Database which picks the database url, username, password and JNDI name from 
environment variables that you can set before starting the bootable jar;

This is very useful because you can use the same `wildfly-bootable-jar-oracle-bootable.jar` and deploy it to your test,
production, cloud etc... environment without recompiling it;