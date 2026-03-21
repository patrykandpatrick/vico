# Issues breakdown: Live time-series line chart

Parent spec: [`../live-time-series-line-chart.md`](../live-time-series-line-chart.md)

Vertical slices (tracer bullets). Create GitHub issues in this order so **Blocked by** references stay valid; after `gh issue create`, paste the issue number into dependent bodies.

| Order | File | Title (short) | Blocked by |
|------:|------|-----------------|------------|
| 1 | [01-live-timeline-follow-controller-and-tests.md](./01-live-timeline-follow-controller-and-tests.md) | Controller + unit tests | — |
| 2 | [02-compose-live-chart-host-integration.md](./02-compose-live-chart-host-integration.md) | Compose host + gestures + vsync | #1 |
| 3 | [03-sample-live-time-series-chart.md](./03-sample-live-time-series-chart.md) | Compose sample | #2 |
| 4 | [04-documentation-live-timeline-chart.md](./04-documentation-live-timeline-chart.md) | Guide docs | #2 |

**03** and **04** can proceed in parallel once **02** lands.

### `gh issue create` example

```bash
gh issue create --title "Add live timeline follow controller with unit tests" --body-file prds/live-time-series-line-chart/01-live-timeline-follow-controller-and-tests.md
```

Repeat for 02–04, updating **Blocked by** sections with real `#` numbers as you go.
