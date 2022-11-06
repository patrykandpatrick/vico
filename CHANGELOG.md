This release includes the following changes.

## Additions

- When a chart marker is shown, the associated list of `Marker.EntryModel`s is now exposed via `MarkerVisibilityChangeListener` (specifically, via the new `onMarkerShown` function). See also the first point under “API changes.”
- `TextComponent` now has a `textAlign` field for controlling the horizontal text alignment.

## API changes

- In `MarkerVisibilityChangeListener`, `onMarkerVisibilityChanged` has been split into `onMarkerShown` and `onMarkerHidden`. See also the first point under “Additions.”

## Dependency updates

See [this commit](https://github.com/patrykandpatryk/vico/commit/d494985a885bacd6720ac41c8d3ba9b09970d489) for information on the dependency updates included in this release.
