# Application behavior and format support

## Viewing

Open calls libinfiniselect with read-only, single-file selection. The library
uses the optional InfiniFolder picker and its Android SAF/source fallback. MIME
is unrestricted so generically labelled STL/G-code remains selectable. Open with and Share register STL and G-code MIME types;
providers labeling files as generic octet-stream or text may require opening from
inside the app. Input content is opened read-only. No broad filesystem permission,
write path, file replacement or network/printer control exists.

Flat lighting is the default. The first upgrade to 0.1.1 selects flat once
because the initial release persisted its startup Gouraud selection; later manual
shading choices are remembered. Surface modes are mesh/wireframe, flat lighting,
and Gouraud vertex lighting. Normals are computed from triangle winding; Gouraud normals are area
weighted at exactly coincident vertex positions, with no crease-angle threshold.
The renderer prefers 24-bit depth, with a 16-bit fallback, and fits clipping planes
to the rotated scene bounds to distinguish closely spaced faces. Exactly coplanar
overlapping geometry can still compete for depth. The ground grid is a visual
reference scaled to the bounds, not a calibrated ruler.
STL is unitless. Source bounds and triangle counts appear under the viewer.
A drag changes yaw and pitch; pinch and +/- change zoom. Reset rotation restores
the default isometric view while Fit resets zoom. Orientation changes retain the
loaded scene and camera. Full activity/process recreation reopens the saved URI
through libinfiniselect using the saved authoritative identity and current access
URI; expired relays can be refreshed when the provider remains available. It
restores preview time and fits the camera.
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
uses at most 2 degrees or 1 mm of arc per segment. Unknown G/M/T codes, non-XY-plane commands and firmware macros are skipped with
a warning. Their parameters do not change the current position or feedrate; later
supported commands continue. Radius-form arcs and malformed supported commands
still report a line-numbered error. Skipped machine-state changes can make this
preview differ from the machine path.

The timeline includes constant-feed motion, retraction and dwell; it omits
acceleration, jerk, heating waits, leveling, firmware transforms, volumetric
extrusion, filament widths, machine limits and actual homing geometry. G0 uses
the current feedrate. Travel is gray, positive-E G1/G2/G3 motion uses the configured
model color, and the head is amber. Toolpaths stay lines in all surface shading
modes. Playback starts at the complete path; **Simulate** starts at zero when at the end
and resumes a paused preview otherwise. The status shows the active segment,
extrusion versus travel/wait, and interpolated XYZ position in millimeters.
**Full path** pauses and reveals the complete path; **Restart** pauses at zero.
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


InfiniFolder links libinfinemesh for STL and G-code thumbnails and seeds InfiniMesh
as their companion viewer when installed. The handoff carries a read-only relay,
the authoritative source identity, and display metadata through libinfiniselect.
The viewer remains independently installable. Android VIEW/SEND registrations
cover STL/G-code MIME aliases. Extension-scoped VIEW filters also accept named
`.stl`, `.gcode`, `.gco`, `.gc`, `.g` and `.ngc` paths reported as generic binary or
plain text, including uppercase suffixes. Opaque provider paths without a suffix
need a specific MIME or the explicit InfiniFolder handoff.
