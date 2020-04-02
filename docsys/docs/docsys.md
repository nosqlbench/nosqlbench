# DocSys Design

## Future

### Front Matter Everything

- Only markdown files which contain front matter will be considered user-facing docs.
- Front matter will specify the namespace and/or names under which the included file should appear
- Front matter will include search meta and related topics
- Front matter will contain all the meta data that any client app needs to create a basic topic index or menu.
- Front matter will specify whether or not to include the markdown in command line help.
- Front matter will specify whether or not to include the markdown in web help.
- Front matter will specify the topic path for the included.

### Internal APIs

- All markdown sources will be provided uniformly behind a markdown service type with SPI, like _MarkdownProvider_
- Services and Static contexts will be supported equally.
- MarkdownProvider data will have digest level info, which is exactly the frontmatter required above.

### Searching

- A standard search index payload in JSON form will be cachable by clients.
- The initial phase of search will contain only meta and topic level matching.
- A subsequent phase may include a compact or compressed form of FTS searching.

