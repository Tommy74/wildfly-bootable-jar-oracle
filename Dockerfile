FROM openjdk:11
COPY ./target/wildfly-bootable-jar-oracle-bootable.jar /usr/src
WORKDIR /usr/src
RUN chgrp -R 0 /usr/src /tmp && chmod -R g=u /usr/src /tmp
USER 1001
CMD ["java", "-jar", "./wildfly-bootable-jar-oracle-bootable.jar"]
