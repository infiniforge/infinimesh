# Validation

Checked 2026-11-18 with JDK 21 on Termux, Android SDK platform 36/build tools
35.0.0 and the system aapt2.

- Debug APK assembled; application ID `com.infinimesh`, version 0.1.0, API 26 minimum.
- App Android lint passed with zero errors and one OldTargetApi advisory because
  target SDK 35 matches the existing suite baseline.
- Standalone library tests: 17 passed, covering ASCII/binary STL, binary `solid`
  headers, bad/truncated/nonfinite input, normals, decoder extensions, resource
  budgets, G-code modes, units, offsets, feeds, dwell, homing warnings and arcs.
- Android library AAR build and lint passed.
- JVM-only core test command passed without configuring the Android module.
- Actual vertex/fragment sources compiled using glslangValidator as GLSL ES 1.00.
- Bundled cube STL: 12 triangles. Bundled spiral G-code: 2,163 segments and
  approximately 76.664 seconds at constant feed.
- Local gasket STL (2,996 triangles) and mount STL (2,156 triangles) decoded and
  built flat and smooth normals. User models are not bundled or published.

Canonical commands:

```sh
./build.sh --max-workers=1 --console=plain
cd libraries/libinfinemesh
./build.sh --max-workers=1 --console=plain
./gradlew :mesh-core:test -PmeshCoreOnly --max-workers=1 --console=plain
python3 scripts/check-shaders.py
```

No ADB device was connected. Handset acceptance remains: Open/Share with grants,
SAF from Infinifolder and another provider, drag/pinch and reset buttons, all three
shading modes on a curved STL, light/dark background and model colors, G-code
scrub/play/pause/restart/speed/travel toggle, rotation/background/resume, and error
handling for revoked grants and large models. Host shader checks do not verify
Android touch handling or GPU driver behavior on a handset.
