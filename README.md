# jaguar
Auto-scaler to slider-managed long running applications in yarn cluster.

### Requirements ###
* Hadoop 2.8 and slider 0.91

### Build Jaguar###
```
git clone https://github.com/TeraprocSoftware/jaguar.git
cd jaguar
mkdir libs
cp slider-0.91.0-incubating-SNAPSHOT/lib/*.jar ./libs
./gradlew clean build
```
