---
name: release-notes
description:
    Use when drafting, reviewing, or prefiling Vico GitHub release notes,
    changelogs, or release bodies. Applies to release notes generated from Git
    history, GitHub releases, issues, discussions, pull requests, and publishing
    workflow context.
---

# Release Notes

## Vico Style

Write GitHub release notes in the established Vico format:

1. Start with `## Overview`.
2. Add lowercase overview bullets in this order when applicable:
    - `breaking changes: none`, `negligible`, `minor`, `moderate`, or `major`
    - `addressed: #123, #456`
    - `external contributors: @username`
3. Follow with only the nonempty implementation sections needed for the release:
    - `## Common` for shared APIs, data models, rendering, markers, axes, and
      behavior spanning Compose and Android Views.
    - ``## `compose` `` for Compose Multiplatform-specific APIs and fixes.
    - ``## `views` `` for Android Views-specific APIs, XML attributes, and
      fixes.
4. Use bullets for short releases. Use numbered lists when a section contains
   several related API changes that build on one another.
5. Prefer concise maintainer verbs: `Added`, `Fixed`, `Improved`, `Introduced`,
   `Renamed`, `Deprecated`, `Exposed`, `Updated`, `Prevented`, and `Relocated`.
6. Format API symbols, modules, parameters, and properties in backticks. Use
   italics for coordinate names such as `_x_` and `_y_`.
7. Mention deprecations, replacements, and compatibility impact where relevant.
   Use a brief footnote only when a breaking-change rationale would otherwise
   interrupt the section.
8. Keep the text concrete and user-facing. Do not list routine dependency bumps,
   formatting, release version commits, CI-only work, or internal refactors
   unless they affect public behavior.
9. Avoid marketing language, "What's Changed" headings, commit hashes, and
   contributor thanks outside the overview metadata.

## Workflow

1. Determine the target tag from the request, `TAG_NAME`, or
   `buildSrc/src/main/kotlin/Versions.kt`.
2. Compare the previous published version tag with the target tag, or with
   `HEAD` if the target tag does not exist yet.
3. Review commits, pull requests, linked issues, discussions, and relevant diffs
   to find user-visible changes.
4. Group each change under the most specific Vico section. If a change touches
   both `vico/compose` and `vico/views` through shared code, place it under
   `Common`.
5. Classify breaking changes conservatively. Use only `none`, `negligible`,
   `minor`, `moderate`, or `major`. Treat source-incompatible public API changes
   as at least `minor`, even when deprecated overloads cover most callers.
6. Write a draft that is ready to paste into a GitHub release body.
7. Verify that the draft starts with `## Overview`, has no empty sections, and
   contains no claims unsupported by the inspected history.

## Template

```markdown
## Overview

- breaking changes: none
- addressed: #123
- external contributors: @username

## Common

- Fixed ...

## `compose`

- Added ...

## `views`

- Exposed ...
```
