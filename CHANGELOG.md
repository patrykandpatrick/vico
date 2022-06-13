This release includes the following changes.

## Additions

- `TextComponent` now allows you to specify the height available for the text.

## Resolved issues

- In some instances, `TextComponent` could measure and draw itself incorrectly when rotated.
- During the measuring phase, `HorizontalAxis` provided `HorizontalAxis#titleComponent` with an incorrect maximum width.
- `VerticalAxis` didn’t limit the length of its title. This could cause the title to stick out past the axis.

## API Changes

- The `includePadding` parameter of `TextComponent#getTextBounds` is now called `includePaddingAndMargins` for clarity.

## Dependency updates

- Vico now targets API level 32.
- The `view` module no longer unnecessarily depends on `com.google.android.material`.
- The `view` and `compose` modules now use `androidx.core:core-ktx` version 1.8.0.
- The `view` and `compose` modules now use `androidx.appcompat:appcompat` version 1.4.2.
- The `compose` module now uses Jetpack Compose version `1.2.0-beta03`.
- The `compose` module now uses `org.jetbrains.kotlinx:kotlinx-coroutines-core` version 1.6.2.
