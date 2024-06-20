#!/bin/bash -e

# export GITHUB_USERNAME=<>
# export GITHUB_TOKEN=<>

PUBLIC_REMOTE=origin
BRANCH=master

git checkout $BRANCH
echo "Pulling current from $PUBLIC_REMOTE/$BRANCH"
git pull $PUBLIC_REMOTE $BRANCH > /dev/null
current_version=$(grep library gradle/libs.versions.toml | sed -e 's/.*"\(.*\)".*/\1/' | head -n 1)

echo "Current version: $current_version"
echo -n "Enter new version: "
read new_version
echo "New version: $new_version"

read -p "Releasing karoo-ext ($new_version): Is this correct? [y/N]" answer
case "$answer" in
   [Yy]* ) ;;
   * ) echo "Aborting karoo-ext release"; exit 1;;
esac

sed -i '' "s/$current_version/$new_version/" gradle/libs.versions.toml
sed -i '' "s/$current_version/$new_version/" lib/src/main/kotlin/io/hammerhead/karooext/Constants.kt
sed -i '' "s/$current_version/$new_version/" lib/Module.md
sed -i '' "s/$current_version/$new_version/" README.md

./gradlew clean
./gradlew lib:assemblerelease
./gradlew app:assemblerelease
./gradlew dokkaHtml
./gradlew publish

# Tagging and pushing
git add gradle/libs.versions.toml lib/Module.md lib/src/main/kotlin/io/hammerhead/karooext/Constants.kt README.md docs
git commit -m "karoo-ext release $new_version"
git tag $new_version

git push $PUBLIC_REMOTE
git push $PUBLIC_REMOTE $new_version

