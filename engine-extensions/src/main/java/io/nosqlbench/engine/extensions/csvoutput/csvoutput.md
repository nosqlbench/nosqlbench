csvoutput extension
===================

This extension makes it easy to start writing CSV data to a file,
using a defined set of headers.

### Examples

Open a writer and write a row:

    var out=csvoutput.open('output.csv','time','value');
    out.write({'time':23,'value':23});

