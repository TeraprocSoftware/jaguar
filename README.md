# jaguar
Auto-scaler for slider-managed long running services in yarn cluster.

### Requirements ###
* Java 1.7+
* python and pip
* Hadoop 2.8 and slider 0.91
* Elasticsearch 1.7.3
* Postgresql 9.4
* [hadoop-metrics-elasticsearch-sink](https://github.com/TeraprocSoftware/hadoop-metrics-elasticsearch-sink)

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

### Install Elasticsearch ###

* Download and install elasticsearch rpm package.
```
sudo rpm -i elasticsearch-1.7.3.noarch.rpm
sudo chkconfig --add elasticsearch
```

* Start elasticsearch service.
```
service elasticsearch start
```

* Elasticsearch conf directory: /etc/elasticsearch/
* Elasticsearch data directory: /var/lib/elasticsearch/
* Elasticsearch log directory: /var/log/elasticsearch/

### Install Postgresql ###

* Download and install postgresql rpm package.
```
sudo yum install -y http://yum.postgresql.org/9.4/redhat/rhel-6-x86_64/pgdg-redhat94-9.4-1.noarch.rpm
sudo yum install -y postgresql94-server postgresql94-contrib
sudo service postgresql-9.4 initdb
sudo chkconfig postgresql-9.4 on
```

* Start postgresql service.
```
sudo service postgresql-9.4 start
```

* Configure user postgres password by executing following commands.
```
sudo su - postgres
$ psql
postgres=# ALTER USER postgres WITH PASSWORD 'postgres';
ALTER ROLE
postgres=# \q
$ exit
```

* Change authentication method to "md5" by editing configuration file "/var/lib/pgsql/9.4/data/pg_hba.conf"
```
# TYPE  DATABASE        USER            ADDRESS                 METHOD

# "local" is for Unix domain socket connections only
local   all             all                                     md5
# IPv4 local connections:
host    all             all             127.0.0.1/32            md5
# IPv6 local connections:
host    all             all             ::1/128                 md5
```

* Restart postgresql service.
```
sudo service postgresql-9.4 restart
```

### Install Jaguar ###
```
sudo rpm -i jaguar-0.4.DEV.noarch.rpm
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
metrics.elasticsearch.url=http://${ES_HOST}:9200
```

####jaguar.conf####
```
server=localhost:9000
```

### Install Jaguar CLI ###
#### Requirements ####
* You must have ```pip``` installed first before installing the Jaguar CLI. Follow https://pip.pypa.io/en/stable/installing/ to install ```pip``` if it is not yet installed in your system.

#### Installation ####
* To install the CLI, simply run the following as ```root```. This will install a ```jaguar``` shell script under ```/usr/local/bin/```.
```
$ cd $JAGUAR_HOME/bin
$ pip install --upgrade setuptools
$ pip install -e .
```

#### Using Jaguar CLI####
* Run ```jaguar --help``` or ```jaguar COMMAND --help``` for detailed information on how to use Jaguar CLI.

### Start Jaguar Server ###
* Run ```jaguar service start``` for console mode or ```jaguar service start --daemon``` for daemon mode.
