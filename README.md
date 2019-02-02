# RoomRecyclerViewExample

This is an example project to show a bug I am been struggling with using RecyclerView, Room and Paging.

## App description

A Room database is populated with 1 000 key/value pairs.
The keys start at 1 and end at 1 000, and the values are all initiated as INITIAL.

The database content is shown in a RecyclerView.

Each displayed element also includes a toggle button;
clicking on it will toggle the value from INITIAL to FINAL and reverse.

## Issue description

When pressing the toggle button at the 155th element,
the displayed value changes from INITIAL to FINAL without any issue.

When doing the same operation at the 243rd element,
pressing the button causes the RecyclerView scrolls down.

The issue repeats itself each time a button is pressed around this position.

## Screen capture

The issue can be observed in this [video](/videos/device-2019-02-02-105434.webm)