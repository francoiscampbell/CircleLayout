![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-CircleLayout-green.svg?style=true)

# CircleLayout
An Android layout for arranging children along a circle

You can customize the following options:
* `cl_centerView`: Set a specific view ID to be in the center of the circle
* `cl_angle`: Choose a specific angle between the children or arrange them equally (default)
* `cl_angleOffset`: Start the circle at an offset in degrees relative to the horizontal axis
* `cl_radius`: Choose a specifoc radius for the circle. Overrides `cl_radiusPreset`
* `cl_radiusPreset`: Either `fitsSmallestChild` or `fitsLargestChild`. Automatically picks a radius that will place either the smallest or the largest child view at the outer boundary (minus any padding) and layout the rest along the same radius
* `cl_direction`: Either `clockwise` or `counterClockwise`

## Installation

Standard installation via Gradle:

dependencies {
    compile 'io.github.francoiscampbell:circlelayout:0.1.4'
}

## Examples

Random widgets and a center view:

![](https://i.imgur.com/h90s89m.png)

    <?xml version="1.0" encoding="utf-8"?>
    <io.github.francoiscampbell.circlelayout.CircleLayout xmlns:android="http://schemas.android.com/apk/res/android"
        xmlns:cl="http://schemas.android.com/apk/res-auto"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:paddingBottom="@dimen/activity_vertical_margin"
        android:paddingLeft="@dimen/activity_horizontal_margin"
        android:paddingRight="@dimen/activity_horizontal_margin"
        android:paddingTop="@dimen/activity_vertical_margin"
        cl:cl_angleOffset="90"
        cl:cl_direction="clockwise"
        cl:cl_centerView="@+id/centerView">
    
        <Switch
            android:id="@+id/centerView"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content" />
    
        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="12"
            android:textColor="@color/testTextColor"
            android:textSize="@dimen/clockTestSize" />
    
        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="1"
            android:textColor="@color/testTextColor"
            android:textSize="@dimen/clockTestSize" />
    
        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="2"
            android:textColor="@color/testTextColor"
            android:textSize="@dimen/clockTestSize" />
    
        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="3"
            android:textColor="@color/testTextColor"
            android:textSize="@dimen/clockTestSize" />
    
        <Button
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Button"/>
    
        <CheckBox
            android:layout_width="wrap_content"
            android:layout_height="wrap_content" />
    
        <SeekBar
            android:layout_width="wrap_content"
            android:layout_height="wrap_content" />
    
    </io.github.francoiscampbell.circlelayout.CircleLayout>

A very easy clock layout:

![](https://i.imgur.com/iU0LLFM.png)


    <?xml version="1.0" encoding="utf-8"?>
    <io.github.francoiscampbell.circlelayout.CircleLayout xmlns:android="http://schemas.android.com/apk/res/android"
        xmlns:cl="http://schemas.android.com/apk/res-auto"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:paddingBottom="@dimen/activity_vertical_margin"
        android:paddingLeft="@dimen/activity_horizontal_margin"
        android:paddingRight="@dimen/activity_horizontal_margin"
        android:paddingTop="@dimen/activity_vertical_margin"
        cl:cl_angleOffset="90"
        cl:cl_direction="clockwise">
    
        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="12"
            android:textColor="@color/testTextColor"
            android:textSize="@dimen/clockTestSize" />
    
        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="1"
            android:textColor="@color/testTextColor"
            android:textSize="@dimen/clockTestSize" />
    
        ...
    
        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="11"
            android:textColor="@color/testTextColor"
            android:textSize="@dimen/clockTestSize" />
    
    
    </io.github.francoiscampbell.circlelayout.CircleLayout>
