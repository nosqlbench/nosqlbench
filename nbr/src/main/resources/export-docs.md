# export-docs

This is the built-in app that allows NB5 to export docs for integration into other systems.
During a build of NB5, this app is used to auto-inject much of the content into the doc site.
Invoking it as `nb5 export-docs` creates (or overwrites) a file called `exported-docs.zip`,
containing the markdown source files for binding functions, bundled apps, specifications, and so on.

Using this mechanism ensures that:
1. NB5 contributors who build drivers, bindings, or other modular pieces can use idiomatic forms of
   documentation, allowing them to work in one code base.
2. Contributors are encouraged to make integrated tests follow a literate programming pattern,
   so that they work as examples as well as verification tools.
3. Users of NB5 will never see documentation which is included in integrated tests, because any
   failing test will prevent a failed build from being published or considered for a release.

This is a relatively new mechanism that will be improved further.

