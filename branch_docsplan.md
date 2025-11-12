# NoSQLBench Documentation Consolidation Plan

## Executive Summary

This plan outlines a strategy to consolidate all NoSQLBench documentation into a single, well-organized location at `/docs/` in the repository root, following the **Documentation Quadrants** (Diátaxis) framework while preserving auto-generated and dynamic documentation elements from binding functions, built-in apps, and adapters.

### Key Architectural Decisions

**Location:** `/docs/` at repository root (not a separate module or repository)

**Core Principles:**
1. **GitHub-Native:** All markdown readable in GitHub/IDE without build step
2. **CommonMark Only:** Pure CommonMark specification compliance for maximum portability
3. **Relative Links:** Inter-document links use relative paths (e.g., `../reference/cli.md`)
4. **Compositional Metadata:** Front matter enables flexible organization independent of file location
5. **Living Documentation:** All code examples are tested and verified automatically
6. **Single Source:** Documentation changes tracked with code changes in same commit/PR

**Benefits:**
- ✅ Browse docs directly in GitHub or any IDE
- ✅ Follow links without building the site
- ✅ Edit in any text editor
- ✅ No context switching between code and docs
- ✅ Documentation stays synchronized with code
- ✅ Zola builds beautiful site from same source files

## Documentation Quadrants Framework

The Diátaxis framework organizes documentation into four distinct modes on two axes:

**The Four Quadrants:**
1. **Tutorials** - Learning-oriented, concrete step-by-step guides for newcomers
2. **How-to Guides** - Problem-solving instructions for users with existing knowledge
3. **Reference** - Technical specifications and API details for lookup
4. **Discussions** - Conceptual explanations that build foundational understanding

**Design Principles:**
1. **Quadrant Separation** - Keep quadrants separate to prevent "documentation collapse" where mixed content confuses readers.
2. **Living Documentation** - Documentation must be testable and verifiable. Many docs, including the uniform workload specification, exist as living documents with testable sections. This ensures documentation remains current, reliable, and accurate as the codebase evolves.
3. **Single Source of Truth** - All documentation lives in `docs/` directory at repository root. No separate documentation repository. Docs are maintained alongside the code they document, with version control aligned with code versions. This ensures documentation changes are part of the same commit/PR as code changes.
4. **Clean Markdown with Relative Links** - All markdown files must be browsable directly in GitHub and IDEs without processing. Use relative links between documents (e.g., `../concepts/bindings.md`). No special markup that breaks plain markdown reading. Front matter is compatible with both Zola and plain viewing.
5. **Compositional Organization** - Each markdown file includes front matter with categorical metadata (quadrant, topic, weight, etc.) that allows a compositor step to organize the site structure independent of the file's physical location. This enables modular ownership while maintaining coherent site navigation.
6. **CommonMark Compliance** - All markdown conforms to the [CommonMark specification](https://commonmark.org/). This ensures portability across tools and renderers with consistent parsing behavior. Avoid proprietary extensions except Zola shortcodes when absolutely necessary, used sparingly.

## Current Documentation Landscape

### 1. Primary Documentation Assets (150+ files)

#### A. Developer Documentation (`/devdocs/` - 51 files)
- Design principles, project structure, Java version guides
- Driver development standards (6 files)
- API documentation (endpoints, error handling)
- NBUI design guide
- Documentation system guides (nb_docs.md, bundled_docs.md, docsys.md)
- RFCs and sketches (11+ files)
- EngineBlock legacy documentation
- Visual assets: SVG, PNG, D2, Graphviz, PlantUML

#### B. Local Planning Documents (`/local/` - 75+ files)
- Strategic design documents (MetricsQL, SQLite metrics, operation synthesis)
- Branch plans (25 files, 798 lines of technical proposals)
- Feature implementation plans

#### C. Top-Level Documentation (13 files)
- README.md, BUILDING.md, CONTRIBUTING.md, CODE_OF_CONDUCT.md
- RELEASE_NOTES.md, PREVIEW_NOTES.md
- MODULES.md, CONVENTIONS.md, DOWNLOADS.md
- Architecture planning documents

#### D. Zola Static Site (`/local/nosqlbench-build-docs/site/`)
Published at docs.nosqlbench.io with structure:
- introduction/ (6 pages)
- getting-started/
- user-guide/ (with advanced-topics/)
- workloads-101/
- reference/ (apps, bindings, drivers, versions, workload_definition)
- dev-guide/ (contributing, how-tos, project-standards)
- blog/ (tutorials: CQL, HTTP/REST quickstarts)
- release-notes/

#### E. Adapter-Specific Documentation
- 7 adapter README files (DataAPI, Pinecone, CQL baseline, etc.)
- 50+ YAML activity definitions with embedded documentation

### 2. Auto-Generated Documentation Infrastructure

#### A. Binding Functions Documentation System
**Source:** Java classes in `virtdata-lib-*` modules with:
- `@Categories` and `@Category` annotations
- Javadoc comments describing function behavior
- Type signatures (e.g., `long -> ThreadNum() -> int`)

**Generation:**
- Exported via `BundledMarkdownExporter`
- Organized by category (state, functional, premade, readers, etc.)
- Output format: `funcref_<category>.md` files

**Example Categories:**
- state - side-effect functions for thread-local variables
- functional - data transformation functions
- premade - pre-configured function chains
- readers - data input functions

#### B. Documentation API Architecture
**Core Classes:**
- `Docs.java` - Main aggregator utility
- `DocsBinder.java` - Binding interface for doc providers
- `DocsNameSpace.java` - Namespace management
- `BundledMarkdownLoader/Manifest` - Markdown loading system
- `BundledMarkdownExporter` - Export system
- `BundledFrontmatterInjector` - Zola front matter injection
- `BundledMarkdownProcessor` - Content processing pipeline

**Pipeline:**
1. Source: Markdown files + Javadoc + annotations
2. Processing: BundledMarkdownProcessor + FrontmatterInjector
3. Export: BundledMarkdownExporter + ZipExporter
4. Publishing: Zola site generator → docs.nosqlbench.io

#### C. SPI-Based Documentation Providers
- Service Provider Interface for extensible documentation
- Auto-discovery of documentation from modules
- Namespace-based organization

### 3. Documentation Tooling Stack
- **Zola** - Static site generator (https://www.getzola.org/)
- **D2** (v0.4.2) - Modern diagram language
- **Markdown** - Primary authoring format with front matter
- **Maven** - Build integration and annotation processing
- **Graphviz, PlantUML** - Additional diagram formats

## Documentation Consolidation Strategy

### Phase 1: Target Repository Structure

**Location:** `/docs/` at repository root (single source of truth)

**Key Principle:** Documentation files can live anywhere in the `docs/` directory hierarchy based on modular ownership. The compositor (Zola build) uses front matter metadata to organize the final site structure, regardless of source file location.

```
/docs/
├── README.md (Documentation overview, contributor guide)
├── _index.md (Site homepage content)
├── config.toml (Zola configuration)
├── themes/ (Zola themes)
├── static/ (Static assets: images, diagrams, etc.)
│
├── tutorials/ (Quadrant 1: Learning-oriented)
│   ├── _index.md
│   ├── getting-started/
│   │   ├── installation.md
│   │   ├── first-run.md
│   │   └── understanding-output.md
│   ├── workloads/
│   │   ├── cql-quickstart.md
│   │   ├── http-rest-quickstart.md
│   │   └── custom-workload.md
│   └── data-generation/
│       ├── simple-bindings.md
│       └── templates.md
│
├── guides/ (Quadrant 2: Problem-solving)
│   ├── _index.md
│   ├── workload-design/
│   │   ├── optimizing-throughput.md
│   │   ├── modeling-traffic.md
│   │   └── phased-workloads.md
│   ├── testing/
│   │   ├── load-testing.md
│   │   └── stress-testing.md
│   ├── metrics/
│   │   ├── understanding-metrics.md
│   │   └── querying-sqlite.md
│   └── troubleshooting/
│       ├── common-errors.md
│       └── debugging.md
│
├── reference/ (Quadrant 3: Technical specifications)
│   ├── _index.md
│   ├── bindings/ (AUTO-GENERATED from virtdata)
│   │   ├── _index.md
│   │   ├── funcref_state.md
│   │   ├── funcref_functional.md
│   │   └── [other categories].md
│   ├── drivers/ (MIXED: auto-gen + manual)
│   │   ├── _index.md
│   │   ├── cql.md
│   │   ├── http.md
│   │   └── [adapter docs].md
│   ├── cli/
│   │   ├── commands.md
│   │   └── options.md
│   ├── workload-yaml/
│   │   ├── schema.md
│   │   ├── bindings.md
│   │   └── statements.md
│   ├── metrics/
│   │   └── metricsql.md
│   └── api/
│       └── rest-endpoints.md
│
├── explanations/ (Quadrant 4: Conceptual understanding)
│   ├── _index.md
│   ├── concepts/
│   │   ├── activities.md
│   │   ├── scenarios.md
│   │   ├── bindings.md
│   │   └── metrics.md
│   ├── architecture/
│   │   ├── overview.md
│   │   ├── engine.md
│   │   ├── drivers.md
│   │   └── virtdata.md
│   └── philosophy/
│       ├── principles.md
│       └── why-nosqlbench.md
│
├── development/ (Developer documentation)
│   ├── _index.md
│   ├── contributing.md
│   ├── building.md
│   ├── architecture/
│   │   ├── modules.md
│   │   └── design-patterns.md
│   ├── guides/
│   │   ├── creating-drivers.md
│   │   ├── binding-functions.md
│   │   └── testing.md
│   └── rfcs/
│       └── [design proposals].md
│
└── release-notes/
    ├── _index.md
    └── [version].md

/nb-apis/nb-api/src/main/java/
└── io/nosqlbench/nb/api/
    └── docsapi/
        ├── Docs.java (Doc aggregation - keep existing)
        ├── DocsBinder.java (Keep existing)
        └── [other doc API classes]

/nbr/src/main/java/
└── io/nosqlbench/api/docsapi/
    └── docexporter/
        ├── BundledMarkdownExporter.java (Keep, update output to /docs/)
        ├── BundledMarkdownProcessor.java (Keep existing)
        └── BundledFrontmatterInjector.java (Enhance for composition metadata)
```

**Front Matter Example (TOML format for Zola):**
```toml
+++
title = "Understanding Binding Functions"
weight = 10
template = "docs-page.html"

# Compositional metadata
[extra]
quadrant = "explanations"
topic = "concepts"
category = "bindings"
tags = ["data-generation", "virtdata", "core-concepts"]
testable = true
+++
```

**Key Benefits:**
1. **Plain Markdown Readable:** Browse `/docs/` in GitHub or any IDE, follow relative links naturally
2. **Modular Ownership:** Teams can own their documentation directories
3. **Flexible Organization:** Compositor reorganizes based on metadata, not file location
4. **Version Control:** Documentation changes tracked with code changes
5. **No Build Required for Reading:** Docs are immediately accessible in source form
6. **CommonMark Compatible:** All markdown works in any CommonMark renderer

### Phase 2: Content Mapping to Quadrants

#### Quadrant 1: Tutorials (Learning-Oriented)
**User State:** "I'm new to NoSQLBench"
**Goal:** Environmental familiarity through hands-on learning

**Content Sources:**
- `/local/nosqlbench-build-docs/site/content/blog/cql-starter/quickstart-cql.md`
- `/local/nosqlbench-build-docs/site/content/blog/http-rest/quickstart-http-rest.md`
- `/local/nosqlbench-build-docs/site/content/getting-started/`
- New: "Your First Workload" tutorial
- New: "Understanding Activity Output" tutorial
- New: "Basic Binding Functions" tutorial

**Structure:**
```
tutorials/
├── _index.md
├── 01-getting-started/
│   ├── installation.md
│   ├── first-run.md
│   └── understanding-output.md
├── 02-basic-workloads/
│   ├── cql-quickstart.md
│   ├── http-rest-quickstart.md
│   └── your-first-custom-workload.md
└── 03-data-generation/
    ├── simple-bindings.md
    └── working-with-templates.md
```

#### Quadrant 2: How-To Guides (Problem-Solving)
**User State:** "I know the basics, need to solve specific problems"
**Goal:** Practical solutions to common tasks

**Content Sources:**
- `/local/nosqlbench-build-docs/site/content/user-guide/`
- `/devdocs/devguide/` (applicable user-facing guides)
- Adapter README files (task-oriented sections)
- New: Migration guides, optimization guides, troubleshooting

**Structure:**
```
guides/
├── _index.md
├── workload-design/
│   ├── optimizing-throughput.md
│   ├── modeling-real-traffic.md
│   ├── using-ratios.md
│   └── phased-workloads.md
├── testing-strategies/
│   ├── load-testing-best-practices.md
│   ├── stress-testing.md
│   └── soak-testing.md
├── data-generation/
│   ├── realistic-distributions.md
│   ├── complex-data-structures.md
│   └── custom-binding-functions.md
├── metrics-and-analysis/
│   ├── understanding-metrics.md
│   ├── exporting-results.md
│   └── querying-metrics-sqlite.md
└── troubleshooting/
    ├── common-errors.md
    ├── performance-issues.md
    └── debugging-workloads.md
```

#### Quadrant 3: Reference (Technical Specifications)
**User State:** "I know what I'm looking for, need exact details"
**Goal:** Precise technical lookup

**Content Sources:**
- **AUTO-GENERATED** Binding functions reference (`funcref_*.md`)
- **AUTO-GENERATED** Driver API documentation
- `/local/nosqlbench-build-docs/site/content/reference/`
- CLI command reference
- YAML schema documentation
- Configuration options
- Metrics reference

**Structure:**
```
reference/
├── _index.md
├── bindings/ (AUTO-GENERATED)
│   ├── _index.md
│   ├── funcref_state.md
│   ├── funcref_functional.md
│   ├── funcref_premade.md
│   ├── funcref_readers.md
│   └── [other categories].md
├── drivers/ (MIXED: Some auto-gen, some manual)
│   ├── _index.md
│   ├── cql.md
│   ├── http.md
│   ├── mongodb.md
│   ├── kafka.md
│   └── [adapter-specific].md
├── cli/
│   ├── commands.md
│   ├── options.md
│   └── examples.md
├── workload-yaml/
│   ├── schema.md
│   ├── bindings.md
│   ├── statements.md
│   └── scenarios.md
├── metrics/
│   ├── metrics-reference.md
│   ├── metricsql-syntax.md
│   └── export-formats.md
├── expressions/
│   ├── expression-api.md
│   └── built-in-functions.md
└── api/
    ├── java-api.md
    └── rest-endpoints.md
```

#### Quadrant 4: Explanations (Conceptual Understanding)
**User State:** "I don't understand how this works"
**Goal:** Build mental models and understanding

**Content Sources:**
- `/local/nosqlbench-build-docs/site/content/introduction/`
- `/devdocs/` (conceptual RFCs and design docs)
- Architecture planning documents
- Core concepts documentation
- Design rationale documents

**Structure:**
```
explanations/
├── _index.md
├── core-concepts/
│   ├── activities.md
│   ├── scenarios.md
│   ├── workload-templates.md
│   ├── binding-system.md
│   └── metrics-system.md
├── architecture/
│   ├── overview.md
│   ├── engine-design.md
│   ├── driver-system.md
│   ├── virtdata-architecture.md
│   └── metricsql-integration.md
├── design-philosophy/
│   ├── principles.md
│   ├── why-nosqlbench.md
│   └── comparison-with-other-tools.md
└── advanced-topics/
    ├── operation-synthesis.md
    ├── workload-synthesis.md
    ├── distributed-testing.md
    └── custom-adapters.md
```

### Phase 3: Developer Documentation
**Separate from User Documentation**

**Location:** `devdocs/` (within documentation module)

**Content Sources:**
- Current `/devdocs/` directory
- CONTRIBUTING.md, BUILDING.md
- Driver development guides
- Architecture RFCs

**Structure:**
```
devdocs/
├── _index.md
├── getting-started/
│   ├── building.md
│   ├── contributing.md
│   └── development-setup.md
├── architecture/
│   ├── overview.md
│   ├── module-structure.md
│   └── design-principles.md
├── guides/
│   ├── creating-drivers.md
│   ├── writing-binding-functions.md
│   ├── documentation-system.md
│   └── testing-standards.md
├── rfcs/ (Design proposals)
│   ├── operation-synthesis.md
│   ├── workload-synthesis.md
│   └── [other RFCs].md
├── standards/
│   ├── code-conventions.md
│   ├── java-versions.md
│   └── api-guidelines.md
└── legacy/
    └── engineblock-migration.md
```

### Phase 4: Implementation Roadmap

#### Step 1: Documentation Root Setup (Week 1)
- [ ] Create `/docs/` directory at repository root
- [ ] Set up Zola configuration (`config.toml`, themes, templates)
- [ ] Configure Zola with Diátaxis-friendly theme
- [ ] Create directory structure for four quadrants (tutorials, guides, reference, explanations)
- [ ] Add development/ and release-notes/ sections
- [ ] Create documentation contributor guide (docs/README.md)
- [ ] Establish front matter standard with compositional metadata
- [ ] Verify all markdown is CommonMark compliant
- [ ] Test that relative links work in GitHub/IDE without build

#### Step 2: Reference Documentation (Week 2) - PRESERVE AUTO-GENERATION
- [ ] Update `BundledMarkdownExporter` to export to `/docs/reference/bindings/`
- [ ] Enhance `BundledFrontmatterInjector` to add compositional metadata
- [ ] Ensure binding function doc generation continues working
- [ ] Verify generated markdown is CommonMark compliant
- [ ] Test generated docs are readable in GitHub without build
- [ ] Migrate existing reference content (CLI, YAML schema, metrics) to `/docs/reference/`
- [ ] Update driver documentation templates with proper front matter
- [ ] Ensure all reference docs use relative links
- [ ] Test full build pipeline: Maven → auto-gen → Zola → static site

#### Step 3: Tutorial Content (Week 3)
- [ ] Migrate existing quickstart guides to `/docs/tutorials/`
- [ ] Convert to CommonMark, add front matter with compositional metadata
- [ ] Use relative links to reference docs (e.g., `../../reference/cli/commands.md`)
- [ ] Create "Your First Workload" tutorial
- [ ] Write "Understanding Output" tutorial
- [ ] Add "Basic Binding Functions" tutorial
- [ ] Verify tutorials are readable in GitHub without build
- [ ] Ensure all tutorials are tested and working

#### Step 4: How-To Guides (Week 4)
- [ ] Migrate existing user guide content to `/docs/guides/`
- [ ] Organize by task categories (workload-design, testing, metrics, troubleshooting)
- [ ] Convert to CommonMark with proper front matter
- [ ] Use relative links for cross-references
- [ ] Extract problem-solving content from developer docs
- [ ] Write missing critical guides (troubleshooting, optimization)
- [ ] Verify guides are readable in GitHub/IDE

#### Step 5: Explanations (Week 5)
- [ ] Migrate introduction/core concepts to `/docs/explanations/`
- [ ] Consolidate architecture documentation
- [ ] Extract conceptual content from RFCs
- [ ] Write design philosophy content
- [ ] Add comparison and "why NoSQLBench" content
- [ ] Ensure CommonMark compliance and relative linking
- [ ] Add compositional metadata to all explanations

#### Step 6: Developer Documentation (Week 6)
- [ ] Migrate devdocs/ content to `/docs/development/`
- [ ] Organize RFCs and design proposals
- [ ] Update CONTRIBUTING.md and BUILDING.md
- [ ] Ensure developer guides are current
- [ ] Use relative links to main documentation
- [ ] Add front matter with appropriate categorization
- [ ] Verify all content is GitHub-readable

#### Step 7: Build Integration & Testing (Week 7)
- [ ] Complete Maven build integration
- [ ] Set up living documentation test infrastructure
  - [ ] Create markdown code block extractor
  - [ ] Implement YAML validation for workload examples
  - [ ] Add CLI command verification
  - [ ] Set up binding function example testing
- [ ] Verify auto-generation still works
- [ ] Test Zola build process
- [ ] Create deployment pipeline
- [ ] Set up preview builds for PRs
- [ ] Configure CI to run documentation tests on every PR

#### Step 8: Migration & Cleanup (Week 8)
- [ ] Verify all relative links work in GitHub and IDEs
- [ ] Validate CommonMark compliance across all docs
- [ ] Test full documentation site build with Zola
- [ ] Update root README.md to point to `/docs/`
- [ ] Remove/archive old documentation locations (`/devdocs/`, etc.)
- [ ] Archive `/local/nosqlbench-build-docs/` after verification
- [ ] Create migration guide for contributors
- [ ] Document the front matter metadata standard
- [ ] Set up documentation preview in CI/CD

## Critical Requirements

### 1. Preserve Auto-Generation
**MUST NOT BREAK:**
- Binding function documentation from virtdata annotations
- Driver documentation from adapter metadata
- API documentation from Javadoc
- Built-in app documentation

**Implementation:**
- Keep existing `BundledMarkdownExporter` infrastructure
- Update output paths to `/docs/reference/bindings/`
- Ensure generated markdown is CommonMark compliant
- Add compositional front matter to generated docs
- Ensure Maven build triggers doc generation
- Maintain SPI-based documentation discovery
- Generated docs must be readable in GitHub without build

### 2. Living Documentation with Testable Sections
**MUST IMPLEMENT:**
- Documentation with executable code examples must be testable
- YAML workload specifications in docs must be validated against schema
- CLI command examples should be verified to work as documented
- Binding function examples should have test coverage
- API endpoint documentation should be validated against actual endpoints

**Implementation:**
- Create doc testing infrastructure (similar to doctest)
- Extract code blocks from markdown for automated testing
- Validate YAML examples during documentation build
- Set up CI pipeline to run doc tests on every PR
- Flag outdated documentation when tests fail
- Include test status indicators in published docs where appropriate

**Benefits:**
- Guarantees documentation accuracy as code evolves
- Prevents documentation drift from reality
- Builds user trust in documentation reliability
- Makes documentation a first-class citizen in the development process

### 3. Single Source of Truth & Clean Markdown
**MUST IMPLEMENT:**
- All documentation lives in `/docs/` at repository root
- No separate documentation repository
- All markdown must be CommonMark compliant
- Markdown files must be readable in GitHub/IDE without processing
- Use relative links between documents (e.g., `../concepts/bindings.md`)
- No proprietary markup that breaks plain markdown reading

**Implementation:**
- Validate CommonMark compliance in CI
- Test relative links in repository browser
- Front matter uses TOML format (Zola-compatible, human-readable)
- Avoid Zola shortcodes except when absolutely necessary
- Document any Zola-specific features clearly

### 4. Compositional Organization
**MUST IMPLEMENT:**
- Each document includes front matter with categorical metadata
- Metadata allows compositor (Zola) to organize site independent of file location
- Modular ownership: teams can own their documentation directories
- Site structure determined by metadata, not directory structure

**Front Matter Requirements:**
```toml
+++
title = "Document Title"
weight = 10  # Ordering within section
template = "docs-page.html"

[extra]
quadrant = "tutorials|guides|reference|explanations|development"
topic = "category-name"
category = "subcategory"
tags = ["tag1", "tag2"]
testable = true|false
+++
```

### 5. Maintain Zola Build Compatibility
- Front matter in TOML format for Zola processing
- Proper weight/ordering for navigation
- Section organization compatible with Zola themes
- Diagram generation (D2, Graphviz) integration
- Templates support compositional metadata

### 6. Version Documentation
- Keep release notes system
- Support multiple version docs (future consideration)
- Clear versioning in doc headers
- Deprecation notices where appropriate

### 7. Developer Workflow
- Documentation changes should be simple
- Clear guidance on where content belongs
- Automated validation where possible
- Local preview capability (Zola server)

## Success Metrics

1. **Single Source of Truth:** All documentation in `/docs/` at repository root
2. **GitHub Readability:** All markdown readable in GitHub/IDE without build or processing
3. **CommonMark Compliance:** 100% of documentation uses standard CommonMark
4. **Relative Links:** All inter-doc links use relative paths and work in GitHub
5. **Quadrant Organization:** Clear separation of tutorials, guides, reference, and explanations
6. **Compositional Metadata:** Every document has front matter enabling flexible site organization
7. **Auto-Generation:** Binding/driver docs still generated correctly to `/docs/`
8. **Living Documentation:** All code examples, YAML specs, and CLI commands tested and verified
9. **Build Process:** Single Maven command builds complete doc site and runs doc tests
10. **Developer Experience:** Contributors can edit docs in any text editor, preview in GitHub
11. **User Experience:** Users can find what they need based on their goal
12. **Documentation Reliability:** Users can trust that examples work as documented

## Future Considerations

### Short-Term (3 months)
- Add search functionality to Zola site
- Implement doc versioning for releases
- Create documentation contribution templates
- Expand living documentation test coverage to all code examples

### Medium-Term (6 months)
- API documentation from OpenAPI/Swagger specs
- Interactive examples with embedded workload runner
- Video tutorials for complex topics
- Advanced link checking and dead link detection

### Long-Term (12 months)
- Multi-language documentation (i18n)
- Community-contributed cookbook recipes
- Integration with example repository
- Documentation metrics (page views, user feedback)

## Appendix A: Documentation Source Inventory

### Top-Level Files (13)
- README.md, BUILDING.md, CONTRIBUTING.md, CODE_OF_CONDUCT.md
- RELEASE_NOTES.md, PREVIEW_NOTES.md
- MODULES.md, CONVENTIONS.md, DOWNLOADS.md
- arch_planning.md, metricsqlite-plan.md, nb_521.md, nb_523.md

### Developer Documentation (`/devdocs/` - 51 files)
- Main developer guide (9 files)
- Driver development standards (6 files)
- API documentation
- Documentation system guides (3 files)
- Sketches & RFCs (11+ files)
- EngineBlock legacy docs (6 files)
- Visual assets (SVG, PNG, D2, DOT, PUML)

### Local Planning (`/local/` - 75+ files)
- Strategic design documents (13 files)
- Branch plans (25 files, 798 lines)
- Feature implementation plans

### Zola Site (`/local/nosqlbench-build-docs/`)
- Complete static site structure
- Introduction, getting-started, user-guide, reference
- Blog/tutorials, release notes
- Customized Abridge theme

### Adapter Documentation
- 7 adapter README files
- 50+ YAML workload definitions

## Appendix B: Key Technologies

- **Zola** - Static site generator (current, https://www.getzola.org/)
- **D2** - Diagram language (v0.4.2)
- **Graphviz/PlantUML** - Diagram generation
- **Maven** - Build system integration with doc testing
- **Java SPI** - Documentation provider discovery
- **Markdown** - Primary authoring format with testable code blocks

## Appendix C: Auto-Generation API Reference

**Core Classes:**
```
io.nosqlbench.nb.api.docsapi.Docs - Main aggregator
io.nosqlbench.nb.api.docsapi.DocsBinder - Binding interface
io.nosqlbench.nb.api.docsapi.DocsNameSpace - Namespace management
io.nosqlbench.api.docsapi.docexporter.BundledMarkdownExporter - Export system
io.nosqlbench.api.docsapi.docexporter.BundledMarkdownProcessor - Content processing
io.nosqlbench.nb.api.docsapi.BundledMarkdownLoader - Loading system
io.nosqlbench.nb.api.docsapi.BundledMarkdownManifest - Manifest management
```

**Annotations:**
```
@Categories - Function category tagging
@Category - Individual category assignment
@ThreadSafeMapper - Thread safety documentation
```

---

## Next Steps

1. Review this plan with team
2. Prioritize phases based on current needs
3. Assign owners for each phase
4. Create tracking issues for implementation tasks
5. Begin with Phase 1: Module Setup

**Plan Version:** 2.0
**Date:** 2025-11-12
**Author:** Claude Code
**Status:** DRAFT - Awaiting Review

**Changelog:**
- v2.0: **Major revision** - Changed from separate module to `/docs/` at repository root
- v2.0: Added design principles: Single Source of Truth, Clean Markdown with Relative Links, Compositional Organization, CommonMark Compliance
- v2.0: Updated target structure to `/docs/` directory accessible in GitHub/IDE
- v2.0: Added front matter compositional metadata requirements
- v2.0: Enhanced all implementation steps to emphasize GitHub readability and relative links
- v2.0: Added new success metrics for GitHub readability, CommonMark compliance, and relative links
- v2.0: Updated critical requirements with new sections for clean markdown and compositional organization
- v1.1: Corrected static site generator from Hugo to Zola (current tool)
- v1.1: Added "Living Documentation" as core design principle
- v1.1: Added Critical Requirement #2 for testable documentation
- v1.1: Enhanced implementation roadmap with doc testing tasks
- v1.1: Updated success metrics to include documentation reliability
- v1.0: Initial plan creation