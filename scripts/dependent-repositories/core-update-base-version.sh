#!/bin/bash
#
# Update the `base` library dependency of the `core-java` repository to the latest version.
#
# Before merging the auto-generated PR please make sure that the `base` dependency version is actually
# higher than the `core-java` library version.

readonly SOURCE_REPOSITORY=https://api.github.com/repos/SpineEventEngine/base
readonly SOURCE_FILE_WITH_VERSION='ext.gradle'
readonly SOURCE_VERSION_VARIABLE='SPINE_VERSION'

readonly TARGET_REPOSITORY=https://api.github.com/repos/SpineEventEngine/core-java
readonly TARGET_FILE_WITH_VERSION='ext.gradle'
readonly TARGET_VERSION_VARIABLE='spineBaseVersion'
readonly TARGET_COMMIT_MESSAGE="Update the dependency to \`base\` *version*."

readonly OWN_FILE_WITH_VERSION='ext.gradle'
readonly OWN_VERSION_VARIABLE='SPINE_VERSION'
readonly OWN_COMMIT_MESSAGE="Update the library version to *version*."

readonly NEW_BRANCH_NAME='update-to-base-*version*'
readonly BRANCH_TO_MERGE_INTO='master'

readonly PULL_REQUEST_TITLE="Update the dependency to \`base\` *version*."
readonly PULL_REQUEST_BODY="Auto-generated PR to update \`base\` library dependency to the latest \
version. \n\nBefore merging please make sure that the \`base\` version is actually higher than \
the \`core-java\` version."
readonly PULL_REQUEST_ASSIGNEE='armiol'

function main() {
    local gitAuthorizationToken="$1"

    chmod +x ./scripts/dependent-repositories/update-dependency-version.sh
    ./scripts/dependent-repositories/update-dependency-version.sh \
        "${gitAuthorizationToken}" \
        "${SOURCE_REPOSITORY}" \
        "${SOURCE_FILE_WITH_VERSION}" \
        "${SOURCE_VERSION_VARIABLE}" \
        "${TARGET_REPOSITORY}" \
        "${TARGET_FILE_WITH_VERSION}" \
        "${TARGET_VERSION_VARIABLE}" \
        "${NEW_BRANCH_NAME}" \
        "${BRANCH_TO_MERGE_INTO}" \
        "${TARGET_COMMIT_MESSAGE}" \
        "${PULL_REQUEST_TITLE}" \
        "${PULL_REQUEST_BODY}" \
        "${PULL_REQUEST_ASSIGNEE}"

    # As the branch already exists, the second script call will push files directly there.
    ./scripts/dependent-repositories/update-dependency-version.sh \
        "${gitAuthorizationToken}" \
        "${SOURCE_REPOSITORY}" \
        "${SOURCE_FILE_WITH_VERSION}" \
        "${SOURCE_VERSION_VARIABLE}" \
        "${TARGET_REPOSITORY}" \
        "${OWN_FILE_WITH_VERSION}" \
        "${OWN_VERSION_VARIABLE}" \
        "${NEW_BRANCH_NAME}" \
        "${BRANCH_TO_MERGE_INTO}" \
        "${OWN_COMMIT_MESSAGE}" \
        "${PULL_REQUEST_TITLE}" \
        "${PULL_REQUEST_BODY}" \
        "${PULL_REQUEST_ASSIGNEE}"
}

main "$@"
