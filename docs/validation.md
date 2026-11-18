# Validation

## 0.1.1

Checked 2026-11-18 on the same toolchain below.

- Debug APK assembly and app lint passed (zero errors; advisory warnings for the
  target SDK, pinned AGP and intentionally retained suite foreground resources).
- 25 JVM tests passed: 17 mesh/STL/G-code, 3 camera/depth and 5 selection-contract.
- A fresh public recursive clone at app `f869b6f`, mesh `21df32c`, and selector
  `bef55c7` passed `./build.sh --offline --max-workers=1 --console=plain` with
  all 133 tasks executed, including APK assembly, app lint and all 25 tests.
- Mesh library AAR and its lint passed. Standalone libinfiniselect tests passed;
  its separate lint task reports two pre-existing WrongConstant diagnostics in
  persistable-grant calls. The pinned library source was not modified.
- Native EGL/GLES2 regression reproduced the old depth collision and passed
  with fitted clip planes on 16-bit and 24-bit depth renderbuffers. It uses the
  actual viewer shaders and clip-range implementation.
- Mesh base PNG and every generated launcher/foreground density retain alpha
  from 0 to 255, with all four corner pixels transparent. The composited icon
  was visually reviewed. Rebuilding artwork requires only this checkout.
- Loading now compiles against pinned libinfiniselect APIs for selection,
  metadata/grant handling, current/durable URI pairing, and worker-thread reads.

No ADB device was connected. Handset acceptance still includes live Folder/SAF
selection, expired-relay restoration, flat-default migration, close-face rendering
on the handset GPU, and G-code controls with real input files. The first upgrade
selects flat once; later manually chosen shading modes remain persistent.

## Initial 0.1.0

Checked 2026-11-18 with JDK 21 on Termux, Android SDK platform 36/build tools
35.0.0 and the system aapt2.

- Debug APK assembled; application ID `com.infinimesh`, version 0.1.0, API 26 minimum.
- A fresh public recursive clone of app commit `83eeb3b`, with library commit
  `5cd687f`, passed `./build.sh --offline --max-workers=1 --console=plain`: all 85
  tasks executed, including APK assembly, lint and all 17 tests. Only the clone
  and documented SDK/toolchain/cache were used; no sibling app or build output.
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
