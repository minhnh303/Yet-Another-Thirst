---
name: port-changes
description: Port new features and bug fixes from another Minecraft-version branch (e.g. 1.20.1, 1.21, 26.1, 26.2) into the currently checked-out branch of this repo by reading the target branch's diff and re-implementing it for the current version's APIs. Use whenever the user says "port", "sync", "backport", "đồng bộ", "chuyển thay đổi", "áp dụng thay đổi từ branch X", "cập nhật branch này theo branch X", or invokes /port-changes <target-branch>, even if they do not name the skill. Does not commit.
---

# port-changes

Bring changes made on a **target branch** (another MC version) into the **current branch**, adapting code to the current version. Invocation: `/port-changes <target-branch>`.

Why not plain cherry-pick/merge: version branches differ in Minecraft/loader APIs, Java version, mappings, and file layout, so a textual patch often applies cleanly yet compiles wrong or reintroduces target-only constructs. Read intent, then re-implement.

## 1. Preflight
- `git branch --show-current` = current branch. Target = argument; if missing, list local branches and ask. Refuse if target == current.
- `git status --short`: if the tree has unrelated uncommitted changes, notify the user and ask before continuing (the port's edits must stay reviewable).
- Verify target exists (`git rev-parse --verify <target>`); prefer the local ref, fall back to `origin/<target>`.

## 2. Find what to port
Version branches diverge from a shared base, and ports are not recorded by merge, so `merge-base` alone over-reports already-ported work.
- `git merge-base HEAD <target>` → base. List `git log --no-merges --reverse <base>..<target>` (oneline).
- For each target commit, check whether it is already present on the current branch: match by subject (`git log HEAD --grep` on the subject / issue number like `#5`) and by checking the touched code in the current tree. Mark each commit: **new**, **already ported**, or **target-only** (version-specific: mod_version bump, mapping/loader bump, docs for that version, `information` content).
- Show the user the **new** list (hash + subject) and proceed on it; ask only if classification is genuinely unclear.

## 3. Understand each new commit
For each new commit run `git show <hash> --stat` then `git show <hash> -- <paths>`. Write down in one line the **intent** (behavior added/fixed), not the diff. Group related commits (feat + follow-up fix) and port them as one unit.

## 4. Re-implement on the current branch
For each unit, find the equivalent location on the current branch (files may be moved/renamed; search by class/method name) and implement the intent using **current-branch APIs**.
- Read neighbouring code on the current branch and imitate its idioms (registry access, networking, capabilities/data attachments, event bus, Mixin targets, resource formats such as `data/` JSON paths and recipe/tag layouts).
- Do not copy target-only imports, method signatures, or Gradle/Java-level features; check `java_version`, `minecraft_version` in `gradle.properties` of both branches when unsure.
- Keep the repo rules from `AGENTS.md`: common code must not import loader APIs (use `IPlatformHelper`); new `gradle.properties` keys must also be added to `expandProps` in `buildSrc/src/main/groovy/multiloader-common.gradle`; Fabric owns its screens/block entities; Immersive Engineering features are not on Fabric.
- Port assets/data/lang/mixin json entries together with the code that needs them (`*.mixins.json`, `META-INF/services`, `mods.toml`/`neoforge.mods.toml`/`fabric.mod.json` only when the commit changed them meaningfully).
- Never bring over: `mod_version`/version bumps, loader/mapping/dependency version bumps, branch-specific README/COMPATIBLE_MODS docs. Changelog: add an entry in the current branch's `changelog.md` style only if the target commit did, using the current branch's version label format.
- If an intent cannot be implemented because the current version lacks an API or feature, skip that part, and report it instead of faking it.
- Comments describe current logic only (no "ported from" notes).

## 5. Verify
- Run `./gradlew build` (or per-loader `:forge:build` `:neoforge:build` `:fabric:build` for loaders present on the branch). Fix compile errors caused by the port. If a failure is pre-existing/unrelated, say so.
- Do not run `runClient`/`runServer` unless asked.
- Do not commit, stage, or switch branches. Leave changes in the working tree for review.

## 6. Report (short)
Table: target commit → status (ported / already present / skipped + reason) → files touched. Then build result and any item needing the user's decision or in-game testing.

