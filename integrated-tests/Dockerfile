# Use an official base image that includes bash, curl, and jq
FROM alpine:3.16

# Install curl and jq
RUN apk add --no-cache curl jq bash

# Make a directory for your application
WORKDIR /app

# Copy the script into the container
COPY *.sh ./
COPY resources/ ./
# Make sure the script is executable
RUN chmod +x ./*.sh

# Execute the script when the container starts
CMD ["./1-config-client.sh"]
