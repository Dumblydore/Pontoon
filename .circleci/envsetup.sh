set +x // dont print the next lines on run script
printenv | tr ' ' '\n' > local.properties
set -x