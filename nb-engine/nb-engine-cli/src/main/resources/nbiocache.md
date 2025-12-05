---
title: "NB Caching"
description: "Doc for nbiocache."
tags:
  - nb-engine
  - docs
audience: developer
diataxis: reference
component: core
topic: architecture
status: live
owner: "@nosqlbench/devrel"
generated: false
---

# NB Caching

nosqlbench provides a means of caching and validating files from remote URLs.
A user specified location is used for caching any file available via HTTP.
sha256 checksum verification is used by default but may be disabled.
The following options are available via the command line to configure this feature:

## Caching Options

Disable caching. Note that by default caching is enabled. It may be turned off with the following parameter

    --disable-nbio-cache

If caching is enabled, nb will use the directory `$HOME/.nosqlbench/nbiocache` to store files.
This location can be user specified with the parameter

    --nbio-cache-dir </user/specified/directory>

If the nb cache is enabled when nb encounters a reference to a file in the format of a URL (ie http or https)
it will first look in the cache to see if that file already exists.
If the file does not exist, or if the file exists but validation is requested and the checksums do not match
the provided URL will be used to download the file. Example:

    test_floatlist: HdfFileToFloatList("https://testeng-assets.s3.us-east-2.amazonaws.com/vector/testdata/vector/hybrid/random_float_100k/random_float_100k.hdf5"

This will check for the existence of the file `random_float_100k.hdf5` in the cache, by default `$HOME/.nosqlbench/nbiocache/vector/testdata/vector/hybrid/random_float_100k/`
in this case. Note that the local filesystem path is derived from the remote URL path.
If the file is not in the cache it will download it from the specified URL.

    --nbio-cache-force-update

This option can be used to force an update to the cache.
If this is specified the remote file will be downloaded regardless of whether a local copy exists.

    --nbio-cache-no-verify

This option can be specified if checksum verification is not desired.
Performing checksum verification is the default behavior.
By default when a file is downloaded a sha256 checksum will be generated and stored in a file in the same directory as the requested file.
The checksum file will be named identically to the requested file but have '.sha256' appended to the filename.
This checksum will be verified against an identically named checksum file in the remote repository.
If the checksums do not match the file will be re-downloaded.
If no remote checksum is found a warning will be generated but the cached file will be returned.

    --nbio-cache-max-retries

The default number of times nb will attempt to download the remote file is 3.
Multiple download attempts may result from unsuccessful downloads defined as either an exception being thrown
during the download process or a failed checksum verification.
This parameter can be used to change the maximum number of download attempts.

