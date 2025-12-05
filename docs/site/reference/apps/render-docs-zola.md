---
title: docs-render-zola
description: Generate a Zola site from the docs-export bundle.
audience: developer
diataxis: reference
tags:
- docs
component: docsys
topic: architecture
status: live
owner: '@nosqlbench/docs'
generated: false
---

# docs-render-zola

`nb5 docs-render-zola` converts the canonical `exported_docs.zip` bundle into a
ready-to-serve Zola project that uses the abridge theme. The command keeps the
canonical export format untouched—metadata and Markdown are preserved exactly as
produced by `docs-export`—and layers the theme/templates needed for static site
generation on top.

## Usage

```bash
nb5 docs-render-zola \
  --export exported_docs.zip \
  --output rendered-docs-zola.zip \
  --zola-bin zola \
  [-f|--force]
```

Options:

- `--export` (default: `exported_docs.zip`) — path to the bundle produced by
  `nb5 docs-export`.
- `--output` (default: `rendered-docs-zola.zip`) — zipfile that will contain the
  generated Zola project or the built static site.
- `--zola-bin` (default: `zola`) — path to the Zola executable used for the
  `zola build` step.
- `--skip-build` — create the themed Zola project without invoking the Zola
  binary. This is useful for CI environments that do not have Zola installed; a
  user can later run `zola build` manually.
- `-f` / `--force` — overwrite an existing output zip if present.

When `--skip-build` is **not** provided (the default), the command runs
`zola build` and the output zip contains the generated `public/` directory
ready to be unzipped onto any static hosting platform.

## Workflow

1. Run `nb5 docs-export --inventory docs/docs_inventory.json --doclint-report target/doclint-report.json`.
2. Run `nb5 docs-render-zola --export exported_docs.zip`.
3. Unzip the resulting `rendered-docs-zola.zip` (or the `public/` folder inside)
   to deploy the abridge-themed site.

If the input bundle is missing, `docs-render-zola` will first run the bundled
`docs-inventory`, `docs-lint`, and `docs-export` commands (with their default
paths) to produce a fresh `exported_docs.zip` before rendering.

## Installing Zola

`docs-render-zola` ships a helper subcommand that downloads Zola, verifies it
with the embedded Sigstore Java library, and caches the binary under
`$XDG_CACHE_HOME/nosqlbench/zola/`:

```bash
nb5 docs-render-zola install \
  --platform x86_64-unknown-linux-gnu \
  --tag latest
```

This command:

- Calls the GitHub Releases API (optionally using `$GITHUB_TOKEN`) to resolve
  the requested tag (`latest` by default).
- Downloads both the tarball and the matching `.sigstore` bundle into the cache.
- Verifies the tarball with the embedded Sigstore Java verifier (Fulcio issuer
  defaults to `https://token.actions.githubusercontent.com`, and the SAN defaults
  to the repo’s workflow metadata).
- Extracts the tarball under
  `~/.cache/nosqlbench/zola/zola-<tag>` (or the cache you choose) and maintains a
  shim (`zola`/`zola.exe`) pointing at the newest version.

Useful options:

- `--repo`, `--platform`, `--tag` — choose a fork, architecture, or tag.
- `--cache-root`, `--link-name` — change where files land and what shim name is
  created.
- `--expected-san`, `--expected-issuer` — tighten the Sigstore certificate
  matching rules, or leave them blank to fall back to the defaults.
- `--force` — re-download artifacts even if cached.

If you need to verify an artifact manually—without performing the install
workflow—you can call the verification mode directly:

```bash
nb5 docs-render-zola install --verify \
  --artifact ~/.cache/nosqlbench/zola/zola-v0.19.0-x86_64-unknown-linux-gnu.tar.gz \
  --bundle ~/.cache/nosqlbench/zola/zola-v0.19.0-x86_64-unknown-linux-gnu.tar.gz.sigstore \
  --expected-san "https://github.com/getzola/zola/.github/workflows/.*@refs/.*" \
  --expected-issuer "https://token.actions.githubusercontent.com"
```

Passing `--expected-san`/`--expected-issuer` lets you constrain the Fulcio
certificate identity if you know exactly which workflow should have produced
the artifact. Leave them blank (`""`) if you only want cryptographic and Rekor
verification.

## Theme Source

`docs-render-zola` clones the [abridge](https://github.com/Jieiku/abridge)
theme during every render. To keep this cohesive but controllable, the command
exposes the following options:

- `--theme-git` / `--theme-ref` — set the repository URL and branch/tag
  (defaults to the abridge GitHub repo and `main`; the command automatically
  falls back to `master` or the default branch if `main` is missing).
- `--theme-cache` — directory where cloned themes are stored
  (defaults to `~/.cache/nosqlbench/zola-themes`).
- `--theme-dir` — bypass git entirely and copy a local abridge checkout (useful
  for air-gapped environments or local theme tweaks).

When a theme directory is copied into the build workspace its `.git` metadata
is stripped, so the final site never embeds history. If cloning fails (for
example, when offline), rerun `docs-render-zola` with `--theme-dir` pointing at
an already-cloned abridge tree.
