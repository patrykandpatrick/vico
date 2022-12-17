This release includes the following changes.

## Additions

- In the `view` module, `TextComponentStyle` now has a `textAlign` attribute.

## Improvements

- Charts now use the GCD of the _x_ values of their entries as their _x_ step.
- The performance of `DashedShape` instances with `DashedShape#fitStrategy` set to `FitStrategy.Resize` has been improved.
- `rememberFadingEdges` and the `FadingEdges` constructor now use 32 dp, not 0 dp, as the default width of fading edges.

## Resolved issues

- `DefaultDiffProcessor` could cause crashes during rapid data set updates.
- `HorizontalAxis` handled fractional _x_ values improperly.
