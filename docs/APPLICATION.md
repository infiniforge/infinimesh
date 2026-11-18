# Application behavior and format support

## Viewing

Open uses Android SAF with all file types so providers with generic MIME types can
still supply STL/G-code. Open with and Share register STL and G-code MIME types;
providers labeling files as generic octet-stream or text may require opening from
inside the app. Input content is opened read-only. No broad filesystem permission,
write path, file replacement or network/printer control exists.

The selected surface mode is mesh/wireframe, flat lighting, or Gouraud vertex
lighting. Normals are computed from triangle winding; Gouraud normals are area
weighted at exactly coincident vertex positions, with no crease-angle threshold.
The ground grid is a visual reference scaled to the bounds, not a calibrated ruler.
STL is unitless. Source bounds and triangle counts appear under the viewer.
A drag changes yaw and pitch; pinch and +/- change zoom. Reset rotation restores
the default isometric view while Fit resets zoom. Orientation changes retain the
loaded scene and camera. Full activity/process recreation reopens the saved URI
when its grant remains available, restores preview time and fits the camera.
Persistent settings own only colors, shading, grid visibility and travel visibility.

## STL

ASCII STL requires complete solid/facet/loop/vertex structure; multiple solids are
accepted and combined. Binary STL validates its unsigned triangle count against
its exact byte length. Encoded face normals and binary attribute/color extensions
are ignored. Geometry supplies flat and smooth normals. Degenerate faces receive
a finite fallback normal. NaN and infinite coordinates are rejected.

## G-code preview dialect

The initial target is common single-extruder Cartesian FDM G-code, with XYZ zero,
E zero, millimeters, absolute positioning and 1200 mm/min as initial assumptions.

| Commands | Behavior |
| --- | --- |
| G0, G1 | Timed linear XYZ/E moves; modal coordinate-only lines; positive F in current units/minute |
| G2, G3 | XY circular and helical I/J arcs, relative center offsets, exact final endpoint |
| G4 | Dwell, P milliseconds or S seconds |
| G17 | XY plane |
| G20, G21 | Inches / millimeters |
| G90, G91 | Absolute / relative positioning; clear the extrusion override |
| M82, M83 | Absolute / relative extrusion override |
| G92 | Change supplied logical XYZ/E coordinates without moving the physical head |
| G28 | Approximate selected XYZ homing axes at zero, with an explicit warning |
| T0 | Single tool |
| M18/84, M104/109/140/190, M106/107, M105/114/115, M201/203/204/205, M400/900 | Accepted machine commands omitted from path/timing, with a warning |
| M117, M118 | Ignore display/log messages |

Semicolon and parenthesized comments, compact words, N line numbers and trailing
checksums are accepted. Checksums are stripped, not validated. Arc tessellation
uses at most 2 degrees or 1 mm of arc per segment. Radius-form arcs, non-XY planes,
multi-tool paths and unknown G/M commands fail with a line-numbered error rather
than silently omitting motion. Future dialects should be explicit decoders.

The timeline includes constant-feed motion, retraction and dwell; it omits
acceleration, jerk, heating waits, leveling, firmware transforms, volumetric
extrusion, filament widths, machine limits and actual homing geometry. G0 uses
the current feedrate. Travel is gray, positive-E G1/G2/G3 motion uses the configured
model color, and the head is amber. Toolpaths stay lines in all surface shading
modes. Playback starts at the complete path; Play restarts at zero when at the end.
The app pauses when backgrounded. Details lists all preview assumptions.

Command interpretation follows the [Marlin linear move](https://marlinfw.org/docs/gcode/G000-G001.html),
[arc](https://marlinfw.org/docs/gcode/G002-G003.html),
[positioning](https://marlinfw.org/docs/gcode/G090.html) and
[extrusion override](https://marlinfw.org/docs/gcode/M082.html) references for the
subset above; this is not a complete Marlin or CNC emulator.

## Resource limits and future work

Initial app budgets are 64 MiB encoded input, 500,000 STL triangles and 1,000,000
preview segments. Available memory may impose a smaller practical limit; failures
report an error and retain the previous model. Decoding and normal/buffer building
run on a single worker and newer loads cancel pending work. The library accepts
caller-provided decode budgets.

Future work: additional format decoder modules, indexed scene geometry, materials,
UV coordinates and texture resources, then texture rendering. No texture support
is claimed by the initial triangle/toolpath API. A decoder registry separates
format selection from the application and leaves room for these additions.
