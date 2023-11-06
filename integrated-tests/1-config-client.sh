#!/bin/bash

# Vars
BASE_URL=${BASE_URL:-http://localhost:8080}
REALM=${REALM:-master}
USER=${USER:-admin}
PASS=${PASS:-admin}
CLIENT_SCOPE_FILE=${CLIENT_SCOPE_FILE:-"resources/client-scope.json"}
CLIENT_FILE=${CLIENT_FILE:-"resources/client.json"}

# Get admin token
token=$(curl -X POST -s "$BASE_URL/realms/$REALM/protocol/openid-connect/token" \
     -H "Content-Type: application/x-www-form-urlencoded" \
     -d "client_id=admin-cli" \
     -d "username=$USER" \
     -d "password=$PASS" \
     -d "grant_type=password" | jq -r '.access_token')

if [ -z "$token" ]; then
    echo "Error getting access token."
    exit 1
fi

client_scope_json=$(cat "$CLIENT_SCOPE_FILE")
client_scope_name="$(echo $client_scope_json | jq -r '.name')"

param_name=$(echo "$client_scope_json" | jq -r -j --arg name "$client_scope_name" '.protocolMappers[] | select(.name==$name) | .config."form.param.name"')
echo $param_name

client_scope_id=$(curl -X GET -s "$BASE_URL/admin/realms/$REALM/client-scopes" \
     -H "Authorization: Bearer $token" \
     | jq -r --arg name "$client_scope_name" '.[] | select(.name==$name) | .id')

if [ -z "$client_scope_id" ]; then
    echo "Creating client scope $client_scope_name..."
    curl -X POST "$BASE_URL/admin/realms/$REALM/client-scopes" \
        -H "Authorization: Bearer $token" \
        -H "Content-Type: application/json" \
        -d "$client_scope_json"

    client_scope_id=$(curl -X GET -s "$BASE_URL/admin/realms/$REALM/client-scopes" \
        -H "Authorization: Bearer $token" \
        | jq -r --arg name "$client_scope_name" '.[] | select(.name==$name) | .id')

else
    echo "Recreating client scope $client_scope_name..."
    curl -X DELETE "$BASE_URL/admin/realms/$REALM/client-scopes/$client_scope_id" \
        -H "Authorization: Bearer $token" \
        -H "Content-Type: application/json"

    curl -X POST "$BASE_URL/admin/realms/$REALM/client-scopes" \
        -H "Authorization: Bearer $token" \
        -H "Content-Type: application/json" \
        -d "$client_scope_json"

    client_scope_id=$(curl -X GET -s "$BASE_URL/admin/realms/$REALM/client-scopes" \
        -H "Authorization: Bearer $token" \
        | jq -r --arg name "$client_scope_name" '.[] | select(.name==$name) | .id')

fi

echo "Client scope: $BASE_URL/admin/master/console/#/$REALM/client-scopes/$client_scope_id/mappers"


client_json=$(cat "$CLIENT_FILE")
client_name="$(echo $client_json | jq -r '.name')"

client_id=$(curl -X GET -s "$BASE_URL/admin/realms/$REALM/clients" \
     -H "Authorization: Bearer $token" \
     | jq -r --arg name "$client_name" '.[] | select(.name==$name) | .id')

if [ -z "$client_id" ]; then
    echo "Creating client $client_name..."
    curl -X POST "$BASE_URL/admin/realms/$REALM/clients" \
        -H "Authorization: Bearer $token" \
        -H "Content-Type: application/json" \
        -d "$client_json"

    client_id=$(curl -X GET -s "$BASE_URL/admin/realms/$REALM/clients" \
        -H "Authorization: Bearer $token" \
        | jq -r --arg name "$client_name" '.[] | select(.name==$name) | .id')

fi

echo "Client: $BASE_URL/admin/master/console/#/$REALM/clients/$client_id/settings"

default_client_scopes=$(curl -X GET -s "$BASE_URL/admin/realms/$REALM/clients/$client_id/default-client-scopes" \
     -H "Authorization: Bearer $token" | jq -c '.[]')

# deleting all client_scopes from client
for scope in $(echo "${default_client_scopes}" | jq -r '.id'); do
    curl -X DELETE "$BASE_URL/admin/realms/$REALM/clients/$client_id/default-client-scopes/$scope" \
         -H "Authorization: Bearer $token"
done

# adding only created client_scope 
curl -X PUT "$BASE_URL/admin/realms/$REALM/clients/$client_id/default-client-scopes/$client_scope_id" \
     -H "Authorization: Bearer $token"
