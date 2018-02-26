#!/bin/bash

# This script uploads the Travis artifacts to Google Cloud Storage.

# Installation of https://github.com/travis-ci/dpl.
gem instal dpl
# Prepare the test and coverage reports for the upload.
mkdir reports

# Find all directories matching path to add to archive. 
BUILD_REPORTS=`find . -type d -path "*build/reports*"`
JACOCO_REPORTS=`find . -type d -path "*build/jacoco*"`

zip -r reports/test-reports.zip $BUILD_REPORTS
zip -r reports/jacoco-reports.zip $JACOCO_REPORTS

# Returns the value for the specified key.
function getProp() {
    grep "${1}" config/gcs.properties | cut -d'=' -f2
}

# Upload the prepared reports to GCS.
dpl --provider=gcs \
    --access-key-id=GOOGX66ER6DXLZH7IKQF \
    --secret-access-key=${GCS_SECRET} \
    --bucket="$(getProp 'artifacts.bucket')" \
    --upload-dir="$(getProp 'artifacts.folder')"/${TRAVIS_BUILD_NUMBER}-${TRAVIS_PULL_REQUEST_BRANCH:-$TRAVIS_BRANCH} \
    --local-dir=reports \
    --skip_cleanup=true
