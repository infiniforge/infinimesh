# InfiniMesh

Standalone, read-only Android 3D model and toolpath viewer in the InfiniForge family.
Version **0.1.0**. The shared backend is **libinfinemesh** (the library name is intentional).

- ASCII and binary STL, including binary headers beginning with `solid`.
- Mesh/wireframe, flat shading and Gouraud shading for surface models.
- Drag rotation, pinch zoom, zoom buttons, reset rotation, and fit/reset zoom.
- Optional ground grid and persistent default background and untextured model colors.
- G-code travel/extrusion preview with a moving head, play/pause/restart, timeline
  scrubbing and 1×/10×/60×/300× playback. Timing is an approximation.
- Android document picker, Open with and single-file Share; no storage permission
  or companion application required. Infinifolder can supply granted content URIs.
- Built-in cube and spiral samples under the options menu.

This application only views files. Other model formats, texture mapping, material
systems and scene hierarchies are planned. STL has no defined physical units;
G-code is normalized to millimeters. No model editing, slicing, exporting or printer
connection is implemented. See [supported commands and limitations](docs/APPLICATION.md).

## Standalone build

Requirements: Git, Python 3, JDK 17 or 21, Android SDK platform 36 and build tools
35.0.0. AGP 8.9.2 and the checksum-pinned Gradle 9.7.1 wrapper are included in build
metadata. Minimum Android version: API 26. GLES 2.0 is required.

```sh
git clone --recurse-submodules https://github.com/infiniforge/infinimesh.git
cd infinimesh
export ANDROID_HOME=/path/to/android-sdk
./build.sh --max-workers=1 --console=plain
```

`build.sh` initializes and verifies the exact library source pin, runs backend
JVM tests and Android lint, assembles the debug APK, and copies it to
`InfiniMesh-debug.apk`.
The Gradle APK also lives under `build/outputs/apk/debug/`.
No sibling app or workspace is required. Set `sdk.dir` in untracked
`local.properties` instead of `ANDROID_HOME` if preferred. On Termux, install
`aapt2`; the build script selects the installed executable automatically.
Initial builds need Maven/Gradle downloads or a populated cache.

Run the backend tests and Android library build independently:

```sh
cd libraries/libinfinemesh
./build.sh --max-workers=1 --console=plain
```

[Documentation](docs/README.md) · [Architecture](docs/architecture.md) ·
[MIT license](LICENSE.md). The launcher cube and samples are original project assets.
