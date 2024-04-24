#!/bin/bash
curl -O https://www2.census.gov/topics/genealogy/2010surnames/names.zip
unzip names.zip
xsv select 'name,rank,count,prop100k,cum_prop100k' Names_2010Census.csv > surnames.csv
perl -pi -e 'if (/^(.+?),(.+)$/) { $_=uc(substr($1,0,1)).lc(substr($1,1)).",".$2."\n"; }' surnames.csv
perl -pi -e 'if (/All other names/) { $_=""; }' surnames.csv
#zip -9 surnames.csv.zip surnames.csv
rm Names_2010Census.xlsx
rm Names_2010Census.csv
rm names.zip
mv surnames.csv ../surnames.csv
