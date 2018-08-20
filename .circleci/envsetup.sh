set +x // don't print the next lines on run script
printenv | tr ' ' '\n' > local.properties
set -x