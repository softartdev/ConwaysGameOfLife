#!/bin/sh

# Decrypt the file
# --batch to prevent interactive command --yes to assume "yes" for questions

gpg --quiet --batch --yes --decrypt --passphrase="$LARGE_SECRET_PASSPHRASE" \
--output ./keystore.properties ./.github/secrets/keystore.properties.gpg

gpg --quiet --batch --yes --decrypt --passphrase="$LARGE_SECRET_PASSPHRASE" \
--output ./my_key_store.jks ./.github/secrets/my_key_store.jks.gpg

gpg --quiet --batch --yes --decrypt --passphrase="$LARGE_SECRET_PASSPHRASE" \
--output ./app/google-services.json ./.github/secrets/google-services.json.gpg

gpg --quiet --batch --yes --decrypt --passphrase="$LARGE_SECRET_PASSPHRASE" \
--output ./fastlane/api-7350020584032910214-328107-d8d3807d1e1a.json ./.github/secrets/api-7350020584032910214-328107-d8d3807d1e1a.json.gpg
