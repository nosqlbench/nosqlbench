# NoSQLBench Documentation

This directory contains all NoSQLBench documentation, built using [Zola](https://www.getzola.org/) static site generator with the [Abridge](https://github.com/Jieiku/abridge) theme.

## Documentation Philosophy

Our documentation follows the [Diátaxis framework](https://diataxis.fr/) with these core principles:

1. **GitHub-Native** - All markdown is readable directly in GitHub/IDE without building
2. **CommonMark Compliant** - Standard markdown for maximum portability
3. **Relative Links** - Inter-document links work in both GitHub and built site
4. **Living Documentation** - Code examples are tested and verified automatically
5. **Single Source of Truth** - Documentation lives with code, versioned together

## Quick Start for Contributors

### Viewing Documentation

**In GitHub/IDE:** Just browse the markdown files. All links work as-is.

**Building Locally:**
```bash
# Install Zola (if not already installed)
# See: https://www.getzola.org/documentation/getting-started/installation/

# Install Abridge theme
cd docs/themes
git clone https://github.com/Jieiku/abridge.git

# Build and serve
cd ..
zola serve  # Visit http://127.0.0.1:1111
```

### Contributing Documentation

1. **Choose the right quadrant** based on content type:
   - `tutorials/` - Learning-oriented, step-by-step guides
   - `guides/` - Problem-solving, task-oriented how-tos
   - `reference/` - Technical specifications and API docs
   - `explanations/` - Conceptual understanding and architecture
   - `development/` - Contributor and developer docs

2. **Create/edit markdown files** using any text editor

3. **Follow front matter standard** (see below)

4. **Use relative links** for cross-references:
   ```markdown
   See [Binding Functions](../reference/bindings/_index.md) for details.
   ```

5. **Test locally** before committing:
   ```bash
   cd docs
   zola build  # Check for errors
   zola serve  # Preview at http://127.0.0.1:1111
   ```

## Front Matter Standard

All documentation files must include TOML front matter:

```toml
+++
title = "Document Title"
description = "Brief description for SEO and navigation"
weight = 10
template = "docs-page.html"
date = 2025-11-12

[extra]
quadrant = "tutorials"  # tutorials|guides|reference|explanations|development
topic = "getting-started"
category = "installation"
tags = ["setup", "prerequisites"]
testable = true  # Set to true if doc contains testable code examples
author = "NoSQLBench Team"
+++
```

### Front Matter Fields

**Required:**
- `title` - Page title (shown in navigation and browser tab)
- `description` - Brief description (SEO, navigation hints)
- `weight` - Ordering within section (lower numbers appear first)

**Recommended:**
- `extra.quadrant` - Which documentation quadrant this belongs to
- `extra.topic` - Broad topic/category
- `extra.tags` - Array of tags for search and organization
- `extra.testable` - Boolean, true if contains executable/testable code

**Optional:**
- `template` - Override default template
- `date` - Publication/update date
- `extra.category` - Sub-category within topic
- `extra.author` - Author attribution

## Using Zola Shortcodes

You can enhance documentation with [Zola shortcodes](https://www.getzola.org/documentation/content/shortcodes/) and [Abridge theme shortcodes](https://abridge.pages.dev/overview-abridge/):

### Standard Zola Shortcodes

**Figure with caption:**
```
{{/* figure(src="/images/diagram.png", alt="System diagram", caption="NoSQLBench architecture") */}}
```

**Embed YouTube video:**
```
{{/* youtube(id="dQw4w9WgXcQ") */}}
```

### Abridge Theme Shortcodes

**Callout/Admonition:**
```
{{/* callout(type="note") */}}
This is an important note that stands out from regular content.
{{/* end */}}
```

Types: `note`, `tip`, `warning`, `danger`, `info`

**Note:** Shortcodes render as plain text in GitHub markdown viewers, maintaining readability.

## Directory Structure

```
docs/
├── config.toml           # Zola configuration
├── _index.md             # Homepage
├── static/               # Static assets
│   ├── images/           # Images (PNG, JPG, SVG)
│   └── diagrams/         # D2 diagram sources
├── themes/               # Zola themes
│   └── abridge/          # Standard Abridge theme
├── tutorials/            # Quadrant 1: Learning-oriented
├── guides/               # Quadrant 2: Problem-solving
├── reference/            # Quadrant 3: Technical specs
│   └── bindings/         # Auto-generated binding function docs
├── explanations/         # Quadrant 4: Conceptual
├── development/          # Developer/contributor docs
└── release-notes/        # Version release notes
```

## Auto-Generated Documentation

Some documentation is generated automatically from code:

- **Binding Functions** (`reference/bindings/`) - Generated from virtdata annotations
- **Driver Docs** (parts of `reference/drivers/`) - Generated from adapter metadata

Do not edit auto-generated files directly. They will be overwritten on next build.

## Writing Guidelines

### 1. Plain Markdown First

Write in standard CommonMark. Files must be readable in GitHub without building:

```markdown
# Good Example

See the [CLI Reference](../reference/cli/commands.md) for details.

## Installation

Run these commands:

\`\`\`bash
mvn clean install
\`\`\`
```

### 2. Relative Links

Always use relative links, not absolute paths:

- ✅ `../reference/cli/commands.md`
- ❌ `/reference/cli/commands.md`
- ❌ `https://docs.nosqlbench.io/reference/cli/commands/`

### 3. Testable Code Examples

If your doc contains code examples, mark `testable = true` in front matter. This enables automated testing to ensure examples stay current.

### 4. Images and Diagrams

Store images in `static/images/` and reference them:

```markdown
![Architecture diagram](/images/architecture.png)
```

For D2 diagrams, store source in `static/diagrams/diagram-name.d2` and commit both source and generated PNG/SVG.

### 5. Cross-Quadrant References

Link between quadrants to guide readers:

```markdown
For conceptual understanding, see [Activities Explained](../../explanations/concepts/activities.md).

For a practical example, check out the [CQL Tutorial](../../tutorials/workloads/cql-quickstart.md).
```

## Documentation Testing

Living documentation is tested automatically:

1. **CommonMark Compliance** - All markdown validates against CommonMark spec
2. **Link Verification** - Relative links checked for correctness
3. **Code Example Testing** - Executable examples run in CI

To run tests locally:

```bash
# Validate CommonMark (requires commonmark-cli)
find docs -name "*.md" | xargs commonmark

# Check links
./scripts/check-doc-links.sh

# Run doc tests (TODO: implement)
mvn test -Dtest=DocTest
```

## Troubleshooting

**Zola build fails:**
- Check front matter syntax (TOML format with `+++` delimiters)
- Verify all referenced files exist
- Look for unclosed shortcodes

**Links broken in built site:**
- Use relative links, not absolute
- Check file paths from current document location
- Remember: `../` goes up one directory level

**Theme not found:**
- Ensure Abridge is cloned in `themes/abridge/`
- Check `config.toml` has `theme = "abridge"`
- Try: `git submodule update --init --recursive`

## Additional Resources

- [Zola Documentation](https://www.getzola.org/documentation/)
- [Abridge Theme Guide](https://abridge.pages.dev/)
- [CommonMark Spec](https://commonmark.org/)
- [Diátaxis Framework](https://diataxis.fr/)

## Questions?

- Open an [issue](https://github.com/nosqlbench/nosqlbench/issues)
- Ask in [discussions](https://github.com/nosqlbench/nosqlbench/discussions)
- Check the [documentation plan](../branch_docsplan.md)
