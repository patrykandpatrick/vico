---
name: release-notes
description:
    Use when drafting, reviewing, or prefiling Vico GitHub release notes,
    changelogs, or release bodies. Applies to release notes generated from Git
    history, GitHub releases, issues, discussions, pull requests, and publishing
    workflow context.
---

# Release notes

## Vico style

Write GitHub release notes in the established Vico format:

1. Start with `## Overview`.
2. Add lowercase overview bullets in this order when applicable:
    - `breaking changes: none`, `negligible`, `minor`, `moderate`, or `major`
    - `addressed: #123, #456`
    - `external contributors: @username`
3. Follow with `## Changes` for user-visible changes.
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
9. Avoid marketing language, “What’s changed” headings, commit hashes, and
   contributor thanks outside the overview metadata.

## Workflow

1. Determine the target tag from the request, `TAG_NAME`, or
   `buildSrc/src/main/kotlin/Versions.kt`.
2. If the target is a stable version and same-version `next` releases exist,
   follow the stable-after-`next` workflow below before inspecting raw history.
3. Compare the previous published version tag with the target tag, or with
   `HEAD` if the target tag does not exist yet.
4. Review commits, pull requests, linked issues, discussions, and relevant diffs
   to find user-visible changes.
5. Group all user-visible changes under `## Changes`.
6. Classify breaking changes conservatively. Use only `none`, `negligible`,
   `minor`, `moderate`, or `major`. Treat source-incompatible public API changes
   as at least `minor`, even when deprecated overloads cover most callers.
7. Write a draft that is ready to paste into a GitHub release body.
8. Verify that the draft starts with `## Overview`, has no empty sections, and
   contains no claims unsupported by the inspected history.

## Stable releases after `next`

Stable releases are usually preceded by one or more same-version `next`
releases, and the stable release is expected to be identical to the final
`next` release. Each `next` release should already have a GitHub release body.

When drafting notes for a stable target such as `v3.2.0`, first look for
same-version `next` tags such as `v3.2.0-next.1`, `v3.2.0-next.2`, and
`v3.2.0-next.3`.

1. Read the GitHub release bodies for all same-version `next` releases in
   ascending order.
2. Combine those changelogs into one stable release body in the Vico format.
   Preserve user-visible content, addressed issues, external contributors, and
   the strongest breaking-change classification.
3. Deduplicate repeated bullets and lightly normalize wording, but do not
   redraft from scratch.
4. Inspect commits, pull requests, and diffs only to verify that the stable tag
   has no user-visible changes after the final `next`, or to fill gaps when a
   `next` release body is missing.
5. If no same-version `next` changelogs are available, use the normal workflow.

## Template

```markdown
## Overview

- breaking changes: none
- addressed: #123
- external contributors: @username

## Changes

- Fixed …
```
