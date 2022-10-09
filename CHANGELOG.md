This release includes the following changes.

## Additions

- You can now customize charts’ initial scroll position (start or end). Optionally, you can configure charts to automatically scroll to this position when a specific condition is met (e.g., a new chart entry is added). This is possible via `ChartScrollSpec`. See also the first point under “API changes.”

## API changes

- The `isHorizontalScrollEnabled` parameter of the `Chart` composable and the `isHorizontalScrollEnabled` field of `BaseChartView` are now deprecated. Use `ChartScrollSpec` instead. See also the first point under “Additions.”

## Dependency updates

Please see [this commit](https://github.com/patrykandpatryk/vico/commit/be76717daffbd2dae9ae24bcf4343d0485a1e51f) for information on the dependency updates included in this release.
