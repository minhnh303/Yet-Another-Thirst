---
name: port-changes
description: Port new features and bug fixes from another Minecraft-version branch (e.g. 1.20.1, 1.21, 26.1, 26.2) into the currently checked-out branch of this repo by reading the target branch's diff and re-implementing it for the current version's APIs. Use whenever the user says "port", "sync", "backport", "đồng bộ", "chuyển thay đổi", "áp dụng thay đổi từ branch X", "cập nhật branch này theo branch X", or invokes /port-changes <target-branch>, even if they do not name the skill. Does not commit.
---

# port-changes

Bring changes made on a **target branch** (another MC version) into the **current branch**, adapting code to the current version. Invocation: `/port-changes <target-branch>`.

Why not plain cherry-pick/merge: version branches differ in Minecraft/loader APIs, Java version, mappings and file layout, so a textual patch often applies cleanly yet compiles wrong or reintroduces target-only constructs. Read intent, then re-implement. The result must be code native to the current branch's Minecraft/loader/dependency versions; a port that only copies the target's lines is a failed port even if it compiles.

## 1. Preflight
- `git branch --show-current` = current branch. Target = argument; if missing, list local branches and ask. Refuse if target == current.
- `git status --short`: if the tree has unrelated uncommitted changes, tell the user and ask before continuing (the port's edits must stay reviewable).
- Verify target exists (`git rev-parse --verify <target>`); prefer the local ref, fall back to `origin/<target>`.

## 2. Find what to port
Version branches diverge from a shared base, and ports are not recorded by merge, so `merge-base` alone over-reports already-ported work.
- `git merge-base HEAD <target>` → base. List `git log --no-merges --reverse <base>..<target>` (oneline).
- For each target commit, check whether it is already present on the current branch: match by subject (`git log HEAD --grep` on the subject / issue number like `#5`) and by checking the touched code in the current tree. Mark each commit: **new**, **already ported**, or **target-only** (version-specific: mod_version bump, mapping/loader bump, docs for that version, `information` content).
- Show the user the **new** list (hash + subject) and proceed on it; ask only if classification is genuinely unclear.

## 3. Understand each new commit
For each new commit run `git show <hash> --stat` then `git show <hash> -- <paths>`. Write down in one line the **intent** (behavior added/fixed), not the diff. Group related commits (feat + follow-up fix) and port them as one unit.

## 4. Build the version profile (before writing code)
The target diff is a spec of behavior, not code to paste. Before porting, record the differences that decide how code must be written here:
- Compare `gradle.properties` of both branches (`git show <target>:gradle.properties`): `minecraft_version`, `java_version`, loader versions, mappings, dependency versions (Create, IE, Fabric API...).
- Note where the current branch already diverges from the target in shared files: `git diff <target> HEAD --stat -- common/ forge/ neoforge/ fabric/` and read the diffs of files the units touch. Divergence = version adaptation already done here; reuse its patterns.
- List the API families the units touch (item data: NBT vs data components; registries/`ResourceLocation`/`Identifier`; networking payloads; capabilities vs data attachments; event classes/bus; rendering/GUI (`GuiGraphics`, render types); codecs; `data/` folder names and JSON schemas for recipes/tags/loot/advancements; Mixin target signatures) and, for each, how the current branch does it.

## 5. Re-implement on the current branch
For each unit, find the equivalent location on the current branch (files may be moved/renamed; search by class/method name) and implement the intent using **current-branch APIs**.
- Write code as if the feature were designed for this version: imitate the current branch's neighbouring idioms from step 4, not the target's.
- Every class, method, field, constructor signature, enum constant, registry key, and JSON key used in ported code must exist in the current version. Verify each one not already used elsewhere on this branch against current-version sources (Minecraft/loader jars in the Gradle cache or decompiled sources, or `~/Projects/Java/Create/`, `~/Projects/Java/ImmersiveEngineering/` checked out at the version this branch depends on). If it cannot be verified, say so; do not guess.
- Do not copy target-only imports, method signatures, annotations, Gradle/Java-level features (language level, record/pattern syntax beyond `java_version`).
- Mixins: re-derive `@At`/`method` targets and injected signatures from the current version's class; never reuse the target's descriptors unchecked.
- Data/resources: rewrite JSON to the current version's folder names and schema; do not copy files byte-for-byte when the format differs.
- Preserve behavior parity: same gameplay result, defaults, config keys, lang keys, and network/save compatibility within this branch. When the current API forces a behavior difference, keep the closest equivalent and report it.
- Keep the repo rules from CLAUDE.md: common code must not import loader APIs (use `IPlatformHelper`); new `gradle.properties` keys must also be added to `expandProps` in `buildSrc/src/main/groovy/multiloader-common.gradle`; Fabric owns its screens/block entities; Immersive Engineering features are not on Fabric.
- Port assets/data/lang/mixin json entries together with the code that needs them (`*.mixins.json`, `META-INF/services`, `mods.toml`/`neoforge.mods.toml`/`fabric.mod.json` only when the commit changed them meaningfully).
- Never bring over: `mod_version`/version bumps, loader/mapping/dependency version bumps, branch-specific README/COMPATIBLE_MODS docs. Changelog: add an entry in the current branch's `changelog.md` style only if the target commit did, using the current branch's version label format.
- If an intent cannot be implemented because the current version lacks an API or feature, skip that part, and report it instead of faking it.
- Comments describe current logic only (no "ported from" notes).

## 6. Verify
- Version audit before building: review `git diff` of the port and check that no hunk is a verbatim copy of a target hunk that touches version-sensitive APIs (step 4 list) without having been checked. Grep the changed files for target-only identifiers/imports found in the target diff that do not exist on the current branch.
- Run `./gradlew build` (or per-loader `:forge:build` `:neoforge:build` `:fabric:build` for loaders present on the branch). Fix compile errors caused by the port. If a failure is pre-existing/unrelated, say so.
- Do not run `runClient`/`runServer` unless asked.
- Do not commit, stage, or switch branches. Leave changes in the working tree for review.

## 7. Report (short)
Table: target commit → status (ported / already present / skipped + reason) → files touched → version adaptations made (API/format replaced, behavior differences). Then build result and any item needing the user's decision or in-game testing.
