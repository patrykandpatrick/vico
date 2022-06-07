In the view system, charts can be customized via XML attributes. You can make more advanced changes programmatically.

## View attributes

### `BaseChartView` attributes

| Name | Description | Type |
| --- | --- | --- |
| `axisStyle` | The style of chart axes. | An [`Axis`](#axis) attribute set. |
| `columnChartStyle` | The style of column charts. | A [`ColumnChartStyle`](#columnchartstyle) attribute set. |
| `lineChartStyle` | The style of line charts. | A [`LineChartStyle`](#linechartstyle) attribute set. |
| `show*Axis` | Whether to show a specific axis. | A boolean. |
| `chartHorizontalScrollingEnabled` | Whether to enable horizontal scrolling. | A boolean. |
| `chartZoomEnabled` | Whether to enable zooming. | A boolean. |

### `ChartView` attributes

`ChartView` has all the attributes of `BaseChartView`, plus the following.

| Name | Description | Value |
| --- | --- | --- |
| `chart` | The chart type. | `column`, `stackedColumn`, or `line`. |

### `ComposedChartView` attributes

`ComposedChartView` has all the attributes of `BaseChartView`, plus the following.

| Name | Description | Value |
| --- | --- | --- |
| `charts` | The chart types. | Any combination of `column`, `stackedColumn`, and `line` (e.g., `column|line`). |

## Attribute sets

### `Axis`

| Attribute | Description | Type |
| --- | --- | --- |
| `axisLineStyle` | The style of the axis line. | A [`LineComponentStyle`](#linecomponentstyle) attribute set. |
| `axisGuidelineStyle` | The style of axis guidelines. | A [`LineComponentStyle`](#linecomponentstyle) attribute set. |
| `axisTickStyle` | The style of axis ticks. | A [`LineComponentStyle`](#linecomponentstyle) attribute set. |
| `axisLabelStyle` | The style of axis labels. | A [`TextComponentStyle`](#textcomponentstyle) attribute set. |
| `axisTickLength` | The length of axis ticks. | A dimension. |
| `axisLabelBackground` | The background for axis labels. | A color or a [`Shape`](#shape) attribute set. |
| `verticalAxisHorizontalLabelPosition` | The horizontal position of the labels on this axis if it is vertical. | `outside` or `inside`. |
| `verticalAxisVerticalLabelPosition` | The vertical position of the labels on this axis is if it vertical. | `top`, `center`, or `bottom`. |
| `labelRotationDegrees` | The rotation of axis labels in degrees. | A floating-point number. |
| `title` | The axis title. | A string or a reference. |
| `showTitle` | Whether to display the axis title. | A boolean or a reference. |
| `titleStyle` | The style of the axis title. | A [`TextComponentStyle`](#textcomponentstyle) attribute set. |

### `ComponentStyle`

| Attribute | Description | Type |
| --- | --- | --- |
| `color` | The background color. | A color. |
| `shapeStyle` | The shape style. | A [`Shape`](#shape) attribute set. |
| `strokeColor` | The stroke color. | A color. |
| `strokeWidth` | The stroke width. | A dimension. |
| `overlayingComponentStyle` | A component with which to overlay this component. | A [`ComponentStyle`](#componentstyle) attribute set. |
| `overlayingComponentPadding` | The padding between this component and the component with which it is overlaid. | A dimension. |

### `ColumnChartStyle`

| Attribute | Description | Type |
| --- | --- | --- |
| `column1Style` | The style for columns whose index in a segment is 3*k* (*k* ∈ *N*). | A [`ComponentStyle`](#componentstyle) attribute set. |
| `column2Style` | The style for columns whose index in a segment is 1 + 3*k* (*k* ∈ *N*). | A [`ComponentStyle`](#componentstyle) attribute set. |
| `column3Style` | The style for columns whose index in a segment is 2 + 3*k* (*k* ∈ *N*). | A [`ComponentStyle`](#componentstyle) attribute set. |
| `columnOuterSpacing` | The spacing between the columns contained in chart segments. | A dimension. |
| `columnInnerSpacing` | The horizontal padding between the edges of chart segments and the columns they contain. | A dimension. |
| `showDataLabels` | Whether to show data labels. | A boolean. |
| `dataLabelStyle` | The style of data labels. | A [`TextComponentStyle`](#textcomponentstyle) attribute set. |
| `dataLabelVerticalPosition` | The vertical position of each data label relative to the top edge of its respective column. | `top`, `center`, or `bottom`. |
| `dataLabelRotationDegrees` | The rotation of data labels in degrees. | A floating-point number. |

### `TextComponentStyle`

| Attribute | Description | Type |
| --- | --- | --- |
| `labelColor` | The text color. | A color. |
| `backgroundStyle` | The background for the label. | A [`ComponentStyle`](#componentstyle) attribute set. |
| `android:padding` | The padding for each edge of the label. | A dimension. |
| `android:padding*` | The padding for a specific edge or edge pair of the label. | A dimension. |
| `android:fontFamily` | The font family. | `sans-serif`, `sans-serif-*`, or an `@font` reference. |
| `android:textFontWeight` | The font weight. | An integer between 100 and 900. |
| `android:fontStyle` | The font style. | `normal` or `italic`. |
| `android:textSize` | The text size. | A dimension. |
| `android:ellipsize` | The text truncation behavior. | `start`, `end`, `middle`, `none`, or `marquee`. |

### `LineChartStyle`

| Attribute | Description | Type |
| --- | --- | --- |
| `line1Spec` | The style for lines whose index in the list of lines in a line chart is 3*k* (*k* ∈ *N*). | A [`LineSpec`](#linespec) attribute set. |
| `line2Spec` | The style for lines whose index in the list of lines in a line chart is 1 + 3*k* (*k* ∈ *N*). | A [`LineSpec`](#linespec) attribute set. |
| `line3Spec` | The style for lines whose index in the list of lines in a line chart is 2 + 3*k* (*k* ∈ *N*). | A [`LineSpec`](#linespec) attribute set. |
| `spacing` | The point spacing. | A dimension. |

### `LineComponentStyle`

| Attribute | Description | Type |
| --- | --- | --- |
| `color` | The background color. | A color. |
| `thickness` | The line thickness. | A dimension. |
| `shapeStyle` | The shape of the line. | A [`Shape`](#shape) attribute set. |
| `strokeColor` | The stroke color. | A color. |
| `strokeWidth` | The stroke width. | A dimension. |

### `LineSpec`

| Attribute | Description | Type |
| --- | --- | --- |
| `color` | The line color. | A color. |
| `gradientTopColor` | The top color of the vertical background gradient. | A color. |
| `gradientBottomColor` | The bottom color of the vertical background gradient. | A color. |
| `pointSize` | The point size. | A dimension. |
| `lineThickness` | The thickness of the line. | A dimension. |
| `pointStyle` | The style of points. | A [`ComponentStyle`](#componentstyle) attribute set. |
| `cubicStrength` | The strength of the cubic bezier curve between each key point on the line. | A fraction. |
| `showDataLabels` | Whether to show data labels. | A boolean. |
| `dataLabelStyle` | The style of data labels. | A [`TextComponentStyle`](#textcomponentstyle) attribute set. |
| `dataLabelVerticalPosition` | The vertical position of each data label relative to its respective point on the line. | `top`, `center`, or `bottom`. |
| `dataLabelRotationDegrees` | The rotation of data labels in degrees. | A floating-point number. |

### `Shape`

| Attribute | Description | Type |
| --- | --- | --- |
| `cornerSize` | The corner size. | A dimension or a fraction. |
| `cornerTreatment` | The corner style. | `rounded` or `cut`. |
| `topStartCornerSize` | The size of the top-start corner. | A dimension or a fraction. |
| `topEndCornerSize` | The size of the top-end corner. | A dimension or a fraction. |
| `bottomStartCornerSize` | The size of the bottom-start corner. | A dimension or a fraction. |
| `bottomEndCornerSize` | The size of the bottom-end corner. | A dimension or a fraction. |
| `dashLength` | The dash length (`0` for no dashes). | A dimension. |
| `dashGapLength` | The dash gap length (`0` for no dashes). | A dimension. |
