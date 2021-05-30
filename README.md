podman pull oracleinanutshell/oracle-xe-11g:latest
podman run -d -p 49161:1521 oracleinanutshell/oracle-xe-11g

jdbc:oracle:thin:@localhost:49161:xe
system
oracle

clone and install https://github.com/wildfly-extras/wildfly-datasources-galleon-pack:
mvn install -DskipTests -Denforcer.skip=true
(needed for <layer>oracle-datasource</layer> and <layer>oracle-driver</layer>)

java -jar target/wildfly-bootable-jar-oracle-bootable.jar
http://localhost:8080/

standalone.xml:
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