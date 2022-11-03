This release includes the following changes.

## Additions

- When a chart marker is shown, the associated list of `Marker.EntryModel`s is now exposed via `MarkerVisibilityChangeListener` (specifically, via the new `onMarkerShown` function). See also the first point under “API changes.”
- `TextComponent` now has a `textAlign` field for controlling the horizontal text alignment.

## API changes

- In `MarkerVisibilityChangeListener`, `onMarkerVisibilityChanged` has been split into `onMarkerShown` and `onMarkerHidden`. See also the first point under “Additions.”
