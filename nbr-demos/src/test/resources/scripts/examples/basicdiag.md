
*command line:*
```bash
# command line
nb script basic_diag.js
```
*scenario script:*
```javascript
// file: basic_diag.js
basic_diag = params.withOverrides({
    "alias" : "basic_diag",
    "driver" : "diag"
});

print('starting activity basic_diag');
scenario.start(basic_diag);
```
*expected output:*
```text
starting activity basic_diag
```
