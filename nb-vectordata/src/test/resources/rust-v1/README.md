# Deterministic Rust fixture

This fixture is emitted by `../rust/generate_fixture.rs` using Rust's standard
library, targeting the binary xvec layout consumed by `vectordata-rs` 1.7.1.
It is checked in so Maven tests do not require Rust, Cargo, `links/`, or a
network connection.

Regenerate it from the repository root:

```text
rustc nb-vectordata/src/test/rust/generate_fixture.rs -o /tmp/nb-vectordata-fixture
/tmp/nb-vectordata-fixture nb-vectordata/src/test/resources/rust-v1
```

The fixture manifest records the normative Rust source baseline. Merkle,
variable-vector, and adversarial fixtures remain separately covered by Java
format tests until a pinned-crate fixture exporter is added.
