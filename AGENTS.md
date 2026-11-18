# InfiniMesh development

Read README.md, docs/APPLICATION.md, docs/architecture.md and the generated
 docs/ECOSYSTEM.md before changes. This is a standalone read-only Android viewer.
Keep parsers, geometry and reusable rendering in libinfinemesh and application
workflows/settings in this repository. Never add another application to this
Gradle build. Retain SAF workflows without Infinifolder. Preserve granted URI
identity. Use libinfiniselect for selection, grant-aware reads and durable source resolution.
Keep library source pins explicit and document supported format subsets.
No writing models, slicing, editing or printer-control features belong here.
Generated ecosystem copies come from ../.github/ecosystem.json and its generator.
