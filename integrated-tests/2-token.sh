#!/bin/bash

BASE_URL=${BASE_URL:-http://localhost:8080}
REALM=${REALM:-master}
USER=${USER:-admin}
PASS=${PASS:-admin}
CLIENT_SCOPE_FILE=${CLIENT_SCOPE_FILE:-"resources/client-scope.json"}
CLIENT_FILE=${CLIENT_FILE:-"resources/client.json"}
PARAM_VALUE=${PARAM_VALUE:-"exampleUser"}


PARAM_NAME=$(cat "$CLIENT_SCOPE_FILE" | jq -r -j '.protocolMappers[0].config."form.param.name"')

CLIENT_NAME="$(cat "$CLIENT_FILE" | jq -r '.name')"


TOKEN=$(curl -s "$BASE_URL/realms/$REALM/protocol/openid-connect/token" \
--header 'Content-Type: application/x-www-form-urlencoded' \
--data-urlencode "client_id=$CLIENT_NAME" \
--data-urlencode 'grant_type=password' \
--data-urlencode "username=$USER" \
--data-urlencode "password=$PASS" \
--data-urlencode "$PARAM_NAME=$PARAM_VALUE" \
| jq -r '.access_token')

PAYLOAD=$(echo $TOKEN | cut -d'.' -f2)
# Adding padding to have a 4 mult
PADDING=$(printf '%s' "$PAYLOAD" | awk '{ PADDING=4 - length%4; if (PADDING == 4) { PADDING=0 }; for (i=0; i<PADDING; i++) printf "="; }')
DECODED_PAYLOAD=$(echo "${PAYLOAD}${PADDING}" | tr '_-' '/+' | base64 -d)


if jq -e . >/dev/null 2>&1 <<<"$DECODED_PAYLOAD"; then
    echo $DECODED_PAYLOAD | jq .
else
    echo "Decoded payload is not valid JSON"
    echo $DECODED_PAYLOAD
fi