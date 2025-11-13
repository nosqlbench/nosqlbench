# Documentation Migration Status

## Completed ✅

### Reference Documentation
- ✅ Auto-generated binding functions reference (`reference/bindings/`)
- ✅ Auto-generated driver documentation (`reference/drivers/`)
- ✅ Auto-generated apps documentation (`reference/apps/`)
- ✅ Workload YAML specification (`reference/workload-yaml/`)
- ✅ Version information (`reference/versions/`)
- ✅ CLI options and scripting (`reference/cli/`)

### Tutorials
- ✅ Getting started tutorials (`tutorials/getting-started/`)
- ✅ CQL quickstart (`tutorials/workloads/cql-quickstart.md`)
- ✅ HTTP quickstart (`tutorials/workloads/http-quickstart.md`)

### Explanations
- ✅ Core concepts (`explanations/concepts/core-concepts.md`)
- ✅ Design principles (`explanations/philosophy/design-principles.md`)
- ✅ Why NoSQLBench (`explanations/philosophy/why-nosqlbench.md`)
- ✅ Community information (`explanations/philosophy/community.md`)

### Guides
- ✅ Metrics guides:
  - Standard metrics (`guides/metrics/standard-metrics.md`)
  - Metrics options (`guides/metrics/metrics-options.md`)
- ✅ Error handling (`guides/workload-design/error-handlers.md`)
- ✅ Troubleshooting guide (`guides/troubleshooting/error-handling.md`)

### Infrastructure
- ✅ Documentation site structure created
- ✅ Zola configuration (`config.toml`)
- ✅ Abridge theme installed
- ✅ Front matter standards established (TOML format with compositional metadata)

## In Progress 🚧

### Guides - User Guide Migration
The following files from `local/nosqlbench-build-docs/site/content/user-guide/` need migration:

**Core Content:**
- `core-activity-params.md` → `guides/workload-design/activity-parameters.md`
- `core-op-fields.md` → `reference/workload-yaml/op-fields.md`
- `op-templates.md` → `reference/workload-yaml/op-templates.md`
- `workloads-intro.md` → `guides/workload-design/introduction.md`
- `names-and-labels.md` → `guides/workload-design/naming-and-labels.md`
- `ssl-options.md` → `guides/workload-design/ssl-configuration.md`

**Advanced Topics** (`user-guide/advanced-topics/`):
- Configuration techniques → `guides/workload-design/configuration/`
- Labeling system → `guides/workload-design/labeling-system.md`
- Performance factoring → `guides/testing/performance-factoring/`
- Scenario scripting → `guides/workload-design/scenario-scripting/`
- Testing at scale → `guides/testing/scale/`
- Timing terms → `reference/concepts/timing-terminology.md`

## Pending 📋

### Workloads-101 (14 files)
These are important tutorial-style guides for workload design:
- `00-designing-workloads.md` → `guides/workload-design/designing-workloads.md`
- `01-op-templates.md` → `tutorials/workload-basics/op-templates.md`
- `02-workload-template-layout.md` → `tutorials/workload-basics/template-layout.md`
- `03-data-bindings.md` → `tutorials/data-generation/bindings-basics.md`
- `04-op-params.md` → `tutorials/workload-basics/op-parameters.md`
- `05-op-tags.md` → `tutorials/workload-basics/op-tags.md`
- `06-op-blocks.md` → `tutorials/workload-basics/op-blocks.md`
- `07-more-op-templates.md` → `tutorials/workload-basics/advanced-templates.md`
- `08-multi-docs.md` → `tutorials/workload-basics/multi-document.md`
- `09-template-params.md` → `tutorials/workload-basics/template-parameters.md`
- `10-stmt-naming.md` → `tutorials/workload-basics/statement-naming.md`
- `11-named-scenarios.md` → `tutorials/workload-basics/named-scenarios.md`
- `99-yaml-diagnostics.md` → `guides/troubleshooting/yaml-diagnostics.md`

### Development Documentation
Files from `local/nosqlbench-build-docs/site/content/dev-guide/`:

**Contributing:**
- `contributing/` → `development/contributing/`

**How-Tos:**
- `how-tos/implement-an-adapter.md` → `development/guides/creating-adapters.md`
- `how-tos/` → `development/guides/`

**Project Standards:**
- `project-standards/` → `development/architecture/`

### Introduction Content (Remaining)
- `download.md` → Update main README or `_index.md`
- `showcase.md` → `explanations/philosophy/showcase.md`

## Testing and Validation 🧪

### To Do:
- [ ] Install Zola static site generator
- [ ] Test Zola build: `cd docs && zola build`
- [ ] Verify relative links work in GitHub
- [ ] Validate CommonMark compliance
- [ ] Check all front matter is properly formatted
- [ ] Test local preview: `cd docs && zola serve`
- [ ] Verify auto-generated docs integration still works

## Next Steps

1. **Continue Migration:** Complete remaining user-guide and workloads-101 content
2. **Dev Docs:** Migrate development documentation
3. **Testing:** Install Zola and test full build
4. **Link Verification:** Ensure all relative links work correctly
5. **Living Documentation:** Set up testing infrastructure for code examples
6. **Cleanup:** Remove old documentation locations after verification

## Migration Guidelines

All migrated files follow these standards:
- **Front Matter:** TOML format with `+++` delimiters
- **Compositional Metadata:** Include `quadrant`, `topic`, `category`, `tags` in `[extra]` section
- **Relative Links:** Use paths like `../../reference/cli/options.md`
- **CommonMark Compliance:** Pure CommonMark markdown
- **Zola Shortcodes:** Standard Abridge shortcodes permitted (gracefully degrade in GitHub)

## Documentation Quadrants

Content is organized by the Diátaxis framework:
- **Tutorials:** Learning-oriented, step-by-step guides
- **Guides:** Task-oriented, problem-solving instructions
- **Reference:** Information-oriented, technical specifications
- **Explanations:** Understanding-oriented, conceptual discussions
- **Development:** Developer-focused documentation

---

Last Updated: 2025-11-13
