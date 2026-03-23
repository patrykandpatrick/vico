# Issue: Sample — live time-series chart demo (Compose)

**Title (for `gh issue create`):** Add Compose sample for live time-series chart

## Parent PRD

[live-time-series-line-chart.md](./live-time-series-line-chart.md)

## Type

**AFK**

## What to build

Add a **Compose sample** chart (under existing `sample/charts/compose` patterns) that demonstrates:

- **Streaming** or simulated **epoch-ms X** line data with **x ≤ now**.
- **Follow** by default; **pause follow** control; **Go live** control.
- **Drag** to browse history; confirm **no fling** behavior manually in PR description or short screen recording note.
- **Programmatic** change of **visible window** (e.g. buttons or dropdown): narrow vs wide span.

Keep **unbounded history** in sample consistent with PRD v1 (maybe cap simulation rate so dev machines don’t melt).

## Acceptance criteria

- [ ] Sample appears in the same **navigation / catalog** pattern as other Compose samples.
- [ ] UI affordances for **pause follow**, **Go live**, and **at least one** programmatic **window duration** change.
- [ ] README or in-sample comment references **OOM risk** if run unbounded for hours.

## Blocked by

- **02-compose-live-chart-host-integration.md** (host API must exist).

## User stories addressed

14, 16
