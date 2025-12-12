# Agents

Use this document to track automation and custom agents related to the Vico project.

## Quick reference for coding agents

- Codebase layout (Gradle multi-module):
  - `vico/` — core library modules:
    - `core` — base charting.
    - `compose`, `compose-m2`, `compose-m3` — Jetpack Compose bindings.
    - `multiplatform`, `multiplatform-m2`, `multiplatform-m3` — KMP variants.
    - `views` — Android Views.
  - `sample/` — sample apps:
    - `app` — Android app.
    - `compose` — Jetpack Compose demo.
    - `multiplatform` — KMP demo.
    - `views` — Android Views demo.
  - Build scripts: root `build.gradle.kts` and per-module `build.gradle.kts`; settings in `settings.gradle.kts`.
- Build & test:
  - Run the full test suite: `./gradlew check`.
  - Assemble Android sample apps (debug): `./gradlew :sample:app:assembleDebug` (other examples: `:sample:compose:assembleDebug`, `:sample:views:assembleDebug`).
- Docs & guidance:
  - Public guide: https://guide.vico.patrykandpatrick.com.
  - Internal handbook: <PROJECT_GUIDANCE_URL> (replace with your org’s link if different).

## Agent usage notes

When documenting or updating agents, capture project-specific guidance, including:
- When and how the agent should be invoked.
- Inputs the agent expects and outputs it produces.
- Constraints, safeguards, and escalation paths.

## How to add an agent

For each agent, include:
1. **Name** – the agent’s identifier.
2. **Purpose** – what the agent is responsible for.
3. **Owner** – who maintains or monitors the agent.
4. **Notes** – any setup details or links to relevant resources.
