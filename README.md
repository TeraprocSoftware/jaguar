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
