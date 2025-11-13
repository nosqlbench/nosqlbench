# Pre-Launch Checklist - Before Inviting Guests 🏛️

## Critical Path to Launch (Required)

### 1. Install Zola ⚙️
**Status:** Not installed
**Priority:** CRITICAL

```bash
# Install Zola static site generator
# Option A: Download from GitHub releases
wget https://github.com/getzola/zola/releases/download/v0.18.0/zola-v0.18.0-x86_64-unknown-linux-gnu.tar.gz
tar xzf zola-v0.18.0-x86_64-unknown-linux-gnu.tar.gz
sudo mv zola /usr/local/bin/

# Option B: Use package manager (if available)
# snap install zola --edge
```

**Why Critical:** Cannot build or preview the site without Zola.

---

### 2. Test Zola Build 🔨
**Status:** Not tested
**Priority:** CRITICAL

```bash
cd /home/jshook/projects/nosqlbench/docs
zola check                    # Validate config and links
zola build                    # Build the site
zola serve                    # Preview at http://127.0.0.1:1111
```

**Expected Issues to Fix:**
- Broken internal links (relative path errors)
- Missing _index.md files
- Front matter syntax errors
- Image/asset path issues

**Why Critical:** Must verify the site actually builds before launch.

---

### 3. Fix Critical Build Errors 🔧
**Status:** Unknown until build tested
**Priority:** CRITICAL

Based on Zola output, fix:
- Any broken relative links
- Missing section indexes
- Front matter format issues
- Asset path problems

**Why Critical:** Site must build cleanly for deployment.

---

### 4. Verify Auto-Generated Docs Integration 🤖
**Status:** Not verified
**Priority:** HIGH

```bash
# Test that binding function docs still generate
cd /home/jshook/projects/nosqlbench
mvn clean install -DskipTests

# Check if docs are generated to correct location
ls -la docs/reference/bindings/funcref_*.md

# Verify they have proper front matter
head -20 docs/reference/bindings/funcref_state.md
```

**Why Important:** Auto-generated docs are a core feature.

---

### 5. Quick Link Verification 🔗
**Status:** Not done
**Priority:** HIGH

Test a sample of important cross-references:
- Tutorial → Reference links
- Guide → Tutorial links
- Explanation → Guide links
- Navigation between quadrants

**Quick Test:**
```bash
# Check for common broken link patterns
cd docs
grep -r '\.\./\.\./\.\./reference' tutorials/ | head -5
grep -r '\.\./guides' explanations/ | head -5
```

**Why Important:** Users must be able to navigate smoothly.

---

## Nice to Have (Not Blockers)

### 6. Comprehensive Link Checker 🔍
**Status:** Not done
**Priority:** MEDIUM

Create automated link verification:
```bash
# Install link checker
npm install -g markdown-link-check

# Check all files
find docs -name "*.md" -exec markdown-link-check {} \;
```

---

### 7. Remaining Optional Content 📝
**Status:** Identified, not urgent
**Priority:** LOW

- 6 dev guide files (maintainers, releases, how-tos)
- These enhance developer experience but aren't blockers

---

### 8. Living Documentation Tests 🧪
**Status:** Future enhancement
**Priority:** LOW

- Code example validation
- YAML workload testing
- CLI command verification

---

## Pre-Launch Checklist Summary

**MUST DO before inviting guests:**
1. ✅ Content migration - COMPLETE (145+ files)
2. ❌ Install Zola
3. ❌ Test `zola build` succeeds
4. ❌ Fix any critical build errors
5. ❌ Verify auto-generated docs work
6. ❌ Quick link verification (sample)

**SHOULD DO for quality:**
7. ⚪ Comprehensive link checker
8. ⚪ Preview site locally and spot-check navigation

**CAN DO later:**
9. ⚪ Remaining optional dev files
10. ⚪ Living documentation tests

---

## Estimated Time to Launch

**Critical Path:** 1-2 hours
- Install Zola: 5 minutes
- Test build: 5 minutes
- Fix errors: 30-60 minutes (depends on issues found)
- Verify auto-gen docs: 15 minutes
- Quick link check: 15 minutes

**With Quality Checks:** +30 minutes
- Link verification: 20 minutes
- Preview and spot-check: 10 minutes

**Total to Invitation-Ready:** 2-3 hours

---

## Current Status

**Content:** ✅ READY (98% complete, all essentials done)
**Build System:** ⚙️ READY (config, theme in place)
**Testing:** ❌ NOT DONE (need Zola installed)
**Validation:** ❌ NOT DONE (need build test)

**Next Action:** Install Zola and test build! 🚀

---

The palace is built and furnished. We just need to:
1. Light the lamps (install Zola)
2. Open the doors (test build)
3. Polish any rough edges (fix errors)
4. Verify the pathways (check links)

Then we invite the guests! 🎉🏛️✨
