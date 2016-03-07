#!/usr/bin/env bash

# source environment
if [[ -e "${JAGUAR_CONF_DIR}/jaguar-env.sh" ]]; then
  . "${JAGUAR_CONF_DIR}/jaguar-env.sh"
else
  echo "ERROR: Cannot execute ${JAGUAR_CONF_DIR}/jaguar-env.sh." 2>&1
  exit 1
fi

# start/stop jaguar server
echo "Starting jaguar server"
# exec "${JAGUAR_HOME}/bin/jaguar" "$@"
# temp start command
java -classpath  ".:${JAGUAR_HOME}/lib/*" com.teraproc.jaguar.JaguarApplication --spring.config.location=${JAGUAR_CONF_DIR}/jaguar.properties --logging.config=${JAGUAR_CONF_DIR}/logback.xml > /dev/null 2>&1 &
