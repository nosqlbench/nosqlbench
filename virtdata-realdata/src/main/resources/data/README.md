## surnames.csv

Contains the contents of the 2010 census data with surnames and frequency,
with the other demographic data removed.
This data contains any value that was seen 100 times or more.

- Name - Surname of a counted person
- rank - rank by total count
- count - Total with this name
- prop100k - Probability per 100k
- cum_prop100k - Cumulative probability per 100k

## female_firstnames.csv

Contains the first names and their frequency as collected in the 1990
census. No newer data on first names has been provided by the census bureau.
This data contains any value that was seen 100 times or more.

- Name - The first name
- Weight - The relative frequency of this first name
- CumulativeWeight

## male_firstnames.csv

Contains the first names and their frequency as collected in the 1990
census. No newer data on first names has been provided by the census bureau.
This data contains any value that was seen 100 times or more.

- Name - The first name
- Weight - The relative frequency of this first name
- CumulativeWeight

## census_places.csv

Contains all the places available in the 2016 US cencus gazatteer:

- USPS - United States Postal Service State Abbreviation
- GEOID	- Geographic Identifier - fully concatenated geographic code (State FIPS and Place FIPS)
- ANSICODE - American National Standards Insititute code
- NAME - Name
- LSAD - Legal/Statistical area descriptor
- FUNCSTATi - Functional status of entity
- ALAND - Land Area (square meters) - Created for statistical purposes only
- AWATER - Water Area (square meters) - Created for statistical purposes only
- ALAND_SQMI - Land Area (square miles) - Created for statistical purposes only
- AWATER_SQMI - Water Area (square miles) - Created for statistical purposes only
- INTPTLAT - Latitude (decimal degrees) First character is blank or "-" denoting North or South latitude respectively
- INTPTLONG - Longitude (decimal degrees) First character is blank or "-" denoting East or West longitude respectively
