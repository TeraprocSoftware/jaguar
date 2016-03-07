# jaguar
Auto-scaler for slider-managed long running services in yarn cluster.

### Requirements ###
* Hadoop 2.8+ and slider 0.91

### Build Jaguar###
#### Options 1 ####
```
- build slider locally and install into maven local
- run ./gradlew clean build
```
#### Options 2 ####
```
- git clone https://github.com/TeraprocSoftware/jaguar.git
- cd jaguar
- mkdir libs
- cp slider-0.91.0-incubating-SNAPSHOT/lib/*.jar ./libs
- run ./gradlew -PlocalJars clean build
```

### Install Jaguar ###
```
sudo rpm -i jaguar-0.4.DEV-1.noarch.rpm
```

### Setup Environment ###
```
export JAGUAR_HOME=/opt/jaguar-0.4.DEV
export JAGUAR_CONF_DIR=/opt/jaguar-0.4.DEV/conf
```

### Configuration ###

####jaguar-env.sh####
```
export JAVA_HOME=${JAVA_HOME}
export JAGUAR_HOME=${JAGUAR_HOME}
```

####jaguar.properties####
```
hadoop.conf.dir=${HADOOP_CONF_DIR}
slider.conf.dir=${SLIDER_CONF_DIR}
jaguar.db.tcp.addr=localhost
jaguar.db.tcp.port=5432
jaguar.db.name=postgres
jaguar.db.user=postgres
jaguar.db.pass=postgres
jaguar.db.hbm2ddl.strategy=update
metrics.elasticsearch.url=http://localhost:9200
```

####jaguar.conf####
```
jaguar_server=localhost:9000
```

### Start Jaguar Server ###
```
${JAGUAR_HOME}/sbin/jaguar-daemon.sh start
```
