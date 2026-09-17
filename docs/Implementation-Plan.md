# Implementation plan

The attached source and captured data define parity; proposed mechanics remain opt-in.
The installed pack is read-only input. No deployment or play-world changes are part of this build.

1. Retain the complete handoff under `reference/handoff`, with original checksums and attribution.
   Bootstrap exact compile dependencies from a local pack after SHA-256 verification, including
   nested Infiniverse. Pin Java 17, Forge 47.4.0, Minecraft 1.20.1 and Gradle 8.8.
2. Consolidate lifecycle under `kncraft`. Keep legacy implementation packages initially so the
   reviewed algorithms and harnesses can be compared directly. Preserve every serialized ID,
   portal generator resource and encounter modifier. Gate optional integrations before class
   loading; reject incompatible versions and duplicate helpers before gameplay starts.
3. Keep the three performance switches and tent switch, import their legacy values once into
   `kncraft-common.toml`, and expose a local compatibility report. Preserve upstream config ownership.
4. Bundle portal definitions and protected Waystone data. Replace the encounter command scheduler
   with idempotent, bounded server events only if necessary for optional-mod support; preserve
   dimension selection, modifier UUIDs, tags and one-time healing.
5. Build and inspect a parity artifact before adding opt-in cotton, wildlife insulation, curated
   meals and cost-equivalent fiber recipes. Use the actual Cold Sweat 2.4.3 API/schema, preserve
   its ownership of insulation NBT and meal consumption, and enforce a shared food-effect cap.
6. Preserve the existing `patchouli:kncraft_guide` ID, all chapters/cookbook entries, templates and
   teal artwork. Bundle data declaration plus resource assets, add gameplay documentation and
   ship a repeatable guide migration procedure. Clients need the book assets; matching mod
   installation on client and server is the supported initial distribution.
7. Retain destructive harnesses as separate artifacts, run deterministic checks and isolated
   dedicated-server validation where feasible. Record physical-client traversal, rendering,
   equipment, containers, feeding and persistence checks separately and honestly.

Acceptance and rollback follow `reference/handoff/docs/Migration-and-Acceptance.md`.
No automatic portal relocation, world migration, upstream upgrade, or world regeneration.
