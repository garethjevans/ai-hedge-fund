#!/usr/bin/env bash

AGENT_NAME=$1
LOWER_AGENT_NAME=`echo "$AGENT_NAME" | tr '[:upper:]' '[:lower:]'`

mkdir agent-$LOWER_AGENT_NAME

mkdir -p agent-$LOWER_AGENT_NAME/src/main/java/org/garethjevans/ai/agent/$LOWER_AGENT_NAME
mkdir -p agent-$LOWER_AGENT_NAME/src/test/java/org/garethjevans/ai/agent/$LOWER_AGENT_NAME

touch agent-$LOWER_AGENT_NAME/src/main/java/org/garethjevans/ai/agent/$LOWER_AGENT_NAME/Agent${AGENT_NAME}Application.java
touch agent-$LOWER_AGENT_NAME/src/main/java/org/garethjevans/ai/agent/$LOWER_AGENT_NAME/Agent${AGENT_NAME}Configuration.java
touch agent-$LOWER_AGENT_NAME/src/main/java/org/garethjevans/ai/agent/$LOWER_AGENT_NAME/Agent${AGENT_NAME}Tool.java
touch agent-$LOWER_AGENT_NAME/src/main/java/org/garethjevans/ai/agent/$LOWER_AGENT_NAME/ServletInitializer.java
mkdir -p agent-$LOWER_AGENT_NAME/src/main/resources/cache
touch agent-$LOWER_AGENT_NAME/src/test/java/org/garethjevans/ai/agent/$LOWER_AGENT_NAME/McpTest.java

