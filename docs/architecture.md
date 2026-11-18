# Architecture

`MeshActivity` owns Android intents, SAF grants, the load worker, viewer controls,
preview clock and private SharedPreferences. ContentResolver metadata and streams
are read on the worker. Input streams are closed by the app; decoder failures are
shown without replacing the last successful document. A generation counter
prevents cancelled loads from replacing newer app state. The activity pauses
playback and the GL surface on pause and cancels work on destruction.

`libinfinemesh:mesh-core` owns immutable source geometry, bounds, normals, STL and
G-code decoders, resource budgets and the decoder extension interface. It has no
Android or application dependency. `mesh-android` owns a reusable GLES 2.0
`MeshView`. It prepares direct buffers on the calling worker and publishes them to
the GL thread. Draw state changes are queued. Surface/context recreation rebuilds
shaders while retaining CPU scene buffers. Coordinates are centered/scaled before
upload to reduce floating-point precision loss; source bounds remain unchanged.

Triangles use flat per-face or smoothed per-position normals. Gouraud lighting is
computed per vertex and interpolated by GLES. Wireframe emits all triangle edges.
G-code keeps source-order line pairs, contiguous travel/print draw batches, timed
binary search and an interpolated head. Host apps control the preview clock.

Gradle consumes the pinned `libraries/libinfinemesh` repository as a composite
build by its public Maven coordinates. App and library build independently. No
application source/classes or private files cross the repository boundary.

To update the backend: commit library changes, update its gitlink and the commit
in `dependencies.lock.json`, and run `build.sh`. Publish library source before app
source so a fresh recursive clone can obtain the pinned revision.
