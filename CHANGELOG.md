## Additions

- chart axis titles

## API changes

- The `TextComponent#rotationDegrees` field has been removed. The rotation of a `TextComponent` is now specified via the `rotationDegrees` parameters of `TextComponent#drawText`, `TextComponent#getWidth`, `TextComponent#getHeight`, and `TextComponent#getTextBounds`. For components that require a `TextComponent` instance, the rotation of the `TextComponent` can be specified via the parent (for example, `Axis` now has a `labelRotationDegrees` field).

## Resolved issues

- Charts that werenʼt scrollable could be rendered incorrectly (#90).

- In right-to-left layouts, `TextComponent` positioned itself incorrectly if `TextComponent#horizontalPosiiton` was equal to `HorizontalPosition.Start` or `HorizontalPosition.End`.