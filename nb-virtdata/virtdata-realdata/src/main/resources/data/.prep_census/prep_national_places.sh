#!/bin/bash
curl -O https://www2.census.gov/geo/docs/maps-data/data/gazetteer/2016_Gazetteer/2016_Gaz_place_national.zip
unzip 2016_Gaz_place_national.zip
mv 2016_Gaz_place_national.txt census_places.csv
perl -pi -e '$_=~s/ *\e */,/g' census_places.csv
perl -pi -e '$_=~s/\s*\n/\n/g' census_places.csv
mv census_places.csv ..
rm 2016_Gaz_place_national.zip


