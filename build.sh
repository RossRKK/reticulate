#clean the existing files

echo "Cleaning"

rm -r deploy/*

#pull the new version of the code

echo "Pulling new version"

cd reticulate

git stash
git pull

#build the source

echo "Building"

cd peer

bash gradlew clean distZip

#extract the distribution

echo "Extracting"

cd ../../

unzip reticulate/peer/build/distributions/peer.zip -d deploy/


