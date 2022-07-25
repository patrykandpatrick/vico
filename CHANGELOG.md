This release includes the following changes.

## Additions

- `HorizontalAxis` now allows for the offset and spacing of centered ticks to be customized. See also the second point under “API changes.”

## API changes

- `ChartEntry` now has a `withY` function. This relates to the second point under “Improvements.”
- `HorizontalAxis.TickType` has been deprecated in favor of `HorizontalAxis.TickPosition`.

## Improvements

- `ChartEntryModelProducer` now allows for a custom `DiffProcessor` implementation to be specified.
- `DefaultDiffProcessor` now works with custom `ChartEntry` implementations.

## Resolved issues

- `DefaultDiffProcessor` incorrectly handled instances where the number of entries decreased.

## Dependency updates

- All modules use Kotlin 1.7.0.
- `compose`, `compose-m2`, and `compose-m3` use version 1.2.0 of the Compose compiler.
- `compose`, `compose-m2`, and `compose-m3` use version `1.2.0-rc03` of Compose.
- `compose-m3` uses version `1.0.0-alpha14` of `androidx.compose.material3:material3`.
