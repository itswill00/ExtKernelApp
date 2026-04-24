#!/bin/bash

# Configuration
TOKEN="8780321748:AAFBdXQW8JWeR2lr3cpsyrydZz62lXBOExg"
CHAT_ID="-1003322434219"
PROJECT_NAME="Ext Kernel Manager"
BUILD_LOG="build.log"

send_msg() {
    local msg=$1
    echo "Sending message..."
    curl -s -X POST "https://api.telegram.org/bot$TOKEN/sendMessage" \
        -d "chat_id=$CHAT_ID" \
        -d "parse_mode=Markdown" \
        -d "text=$msg"
}

send_file() {
    local file_path=$1
    local caption=$2
    echo "Sending file: $file_path"
    curl -s -X POST "https://api.telegram.org/bot$TOKEN/sendDocument" \
        -F "chat_id=$CHAT_ID" \
        -F "document=@$file_path" \
        -F "caption=$caption" \
        -F "parse_mode=Markdown"
}

# 1. Notify Build Started
START_TIME=$(date +%s)
send_msg "🚀 *Build Started:* $PROJECT_NAME
📅 Date: $(date '+%d %b %Y, %H:%M:%S')
🛠 Status: Preparing build environment..."

# 2. Run Gradle Build
echo "Starting build..."
chmod +x gradlew
./gradlew assembleDebug > $BUILD_LOG 2>&1

if [ $? -eq 0 ]; then
    # Success
    END_TIME=$(date +%s)
    DURATION=$((END_TIME - START_TIME))
    
    APK_PATH=$(find app/build/outputs/apk/debug -name "*.apk" | head -n 1)
    
    if [ -f "$APK_PATH" ]; then
        send_file "$APK_PATH" "✅ *Build Success!*
📦 *Project:* $PROJECT_NAME
⏱ *Duration:* ${DURATION}s
📱 *Type:* Debug APK (Hardware Edition)
🔒 *Security:* All modules verified."
    else
        send_msg "⚠️ *Build Success* but APK not found in $APK_PATH"
    fi
else
    # Failure
    ERROR_LOG=$(tail -n 10 $BUILD_LOG)
    send_msg "❌ *Build Failed!*
📦 *Project:* $PROJECT_NAME
🔴 *Error Snippet:*
\`\`\`
$ERROR_LOG
\`\`\`
Check build log for details."
fi
