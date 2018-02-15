# Task
Our task was to create a simple query language similar to the well-known SQL but using only the SELECT
command. The query then filtered data written in an XML file which was our input. The result was then written
into an output XML file. This program acts as a script. My implementation is mostly build on regular
expressions making it much shorter than other versions.

**USAGE**

**--help** Prints help.
**--input** Input file with an XML structure.
**--output** Output file with filtered data.
**--query** Query on data.
**--qf** Query in a file.
**-n** Dont generate a header for our output XML.
**--root** Name of a root element to enclose the results.

## Examples

python3.5 ./xqr.py --input=test.in --query="SELECT book FROM ROOT WHERE price< 20"

python3.5 ./xqr.py --input=test.in --output=out --qf=input

input

SELECT book FROM ROOT WHERE price < 5
