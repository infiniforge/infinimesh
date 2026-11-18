# InfiniMesh launcher artwork

`mesh.svg` is the original faceted model base; `mesh.png` is its 512px transparent
PNG export. Neither has a background rectangle. `infini.svg` is the existing
InfiniForge rainbow infinity emblem, copied unchanged from InfiniFolder commit
`f8f2b1b8a857db127b6570bcc120ae0f86003816`.

`scripts/build-app-icons.py` adapts that same commit's suite launcher compositor:
black silhouette outline, centered infinity emblem and shadow, launcher densities
48/72/96/144/192px and foreground densities 108/162/216/324/432px. The canvas stays
transparent. Like InfiniFolder, launcher and round aliases use full artwork and
let the Android launcher apply its mask rather than permanently clipping it.

Regenerate from this repository alone with Python 3 and librsvg/rsvg-convert:

```sh
python3 scripts/build-app-icons.py
rsvg-convert -w 512 -h 512 -o artwork/mesh.png artwork/mesh.svg
```

Composited source SVGs are regenerated under the ignored
`build/branding/infinimesh/` directory; Android PNG resources are checked in under
`src/main/res/mipmap-*`. APK builds do not run this generator or
read sibling application source. The shared emblem and adapted compositor retain
the suite project attribution; the new base uses this project's MIT terms. No AI
image generation is involved.
