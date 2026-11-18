# Architecture

`MeshActivity` owns Android intents, the load worker, viewer controls, preview
clock and private SharedPreferences. `InfiniSelect.open` owns the selection flow;
`InfiniSelect.readResult` and `StorageAccess.persist` handle returned metadata and
grants. `InfiniSelect.requireSelectedSource` resolves a current read handle paired
with the authoritative resource identity; `StorageAccess.openRead` supplies its
stream. Selection metadata, provider resolution and stream reads run on the worker.
Saved state retains both URIs plus name/MIME hints. Temporary relay handles are
never promoted to durable resource identities; provider failures remain errors. Input streams are closed by the app; decoder failures are
shown without replacing the last successful document. A generation counter
prevents cancelled loads from replacing newer app state. The activity pauses
playback and the GL surface on pause and cancels work on destruction.

`libinfinemesh:mesh-core` owns immutable source geometry, bounds, normals, STL and
G-code decoders, resource budgets and the decoder extension interface. It has no
Android or application dependency. `mesh-android` owns a reusable GLES 2.0
`MeshView`. It prepares direct buffers on the calling worker and publishes them to
the GL thread. Draw state changes are queued. Surface/context recreation rebuilds
shaders while retaining CPU scene buffers. A custom EGL chooser prefers 24-bit
depth and falls back to 16 bits. Clip planes fit a rotation-invariant sphere of
the normalized model and visible grid, avoiding the original almost-zero near
plane and excessive depth range. Flat shading is the initial surface mode. Coordinates are centered/scaled before
upload to reduce floating-point precision loss; source bounds remain unchanged.

Triangles use flat per-face or smoothed per-position normals. Gouraud lighting is
computed per vertex and interpolated by GLES. Wireframe emits all triangle edges.
G-code keeps source-order line pairs, contiguous travel/print draw batches, timed
binary search and an interpolated head. Host apps control the preview clock.

Gradle consumes pinned `libraries/libinfinemesh` and `libraries/libinfiniselect`
repositories as composite builds by their public Maven coordinates. App and library build independently. No
application source/classes or private files cross the repository boundary.

To update the backend: commit library changes, update its gitlink and the commit
in `dependencies.lock.json`, and run `build.sh`. Publish library source before app
source so a fresh recursive clone can obtain the pinned revision.
