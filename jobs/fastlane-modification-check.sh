#!/usr/bin/env bash
set +x
diffContent=$(git diff $GIT_ORIGIN_BRANCH^ $GIT_ORIGIN_BRANCH)
diffSearchString="fastlane[^/metadata]\|FASTLANE_USER\|FASTLANE_PASSWORD\|FASTLANE_DONT_STORE_PASSWORD\|FASTLANE_TEAM_ID\|CODE_SIGNING_IDENTITY\|CERT_PASSWORD\|KEYCHAIN_NAME|\KEYCHAIN_PASSWORD\|DELIVER_PASSWORD\|DELIVER_USER"
RED_COLOR='\033[0;31m'
NO_COLOR='\033[0m'
echo "$diffContent" | grep -q $diffSearchString
set -x
if [ $? -eq 0 ]; then
  echo -e "$RED_COLOR" "Unfortunately you do not have permission to change build scripts. Contact your system admin if you think you should be able to run this build." "$NO_COLOR"
  exit 1
else
  exit 0
fi