# AGENTS.md

Instructions for AI coding agents working in this repository.

## Repository purpose

Java SDK for Hyland CIC services: modular HTTP clients, extensible
JSON serialization/mapping, and high-level APIs for Ingest, Agent, QnA,
Nucleus, Knowledge Enrichment and Data Curation. See `README.md` for the
architecture diagram and per-layer documentation.

## Module layout

Multi-module Maven reactor, root `pom.xml` (`packaging=pom`):

- `cic-http-client` — shared foundation: `AbstractHttpClient` /
  `AbstractHttpClientBuilder` (+ authenticated variants), retry/backoff
  (`RetryPolicy`, `BackoffStrategy`, `RetryCondition`), the `CICObject` /
  `CICArray` / `CICNode` mapper-object API, `CICError`, pagination helpers.
  Every other module depends on it; generic behavior changes here affect
  all modules — call that out explicitly in the PR description.
- `cic-http-client-jackson2` — Jackson2-based `SerializerFactory`
  implementation plugged into `cic-http-client`.
- `cic-ingest`, `cic-agent`, `cic-qna`, `cic-nucleus`, `cic-ke` — one
  module per CIC product area; a module may host several API surfaces
  (e.g. `cic-ke` covers both KE and Data Curation). Each surface follows
  the same internal shape: `XxxHttpClient` (wire/auth layer) →
  `XxxService` (higher-level API) → `object/` (public POJOs/records) →
  `mapper/` (`CICObject`/`CICArray` ⇄ POJO conversion), with a single
  per-module `XxxMapperFactory` registering all mappers.
- New modules extend `AbstractAuthenticatedHttpClient` /
  `AbstractAuthenticatedHttpClientBuilder` from `cic-http-client`.

## Build, format, test

- JDK version is set via `maven.compiler.release` in the root `pom.xml`
  (currently 17).
- Build/install everything: `mvn --batch-mode -nsu install`
- Format (required before committing — Spotless runs in the `validate`
  phase and fails the build if unformatted): `mvn spotless:apply`
  - Config: `sdk-formatter.xml` (Eclipse formatter) + `sdk.importorder`
    (import order), wildcard imports forbidden, unused imports removed.
  - If Spotless can't produce readable multi-line array/constant
    layouts, prefer adding a trailing `//` comment on each line over
    fighting the formatter config.
- E2E tests live behind the `e2e` Maven profile (`maven-failsafe-plugin`),
  run per-module, e.g.: `mvn --batch-mode -nsu verify -Pe2e -pl cic-ke -am`
  - They hit real CIC staging endpoints via module-prefixed secrets
    (e.g. `CIC_KE_*`); keep secret/env-var naming scoped to the module
    that actually uses them, don't reuse generic names across modules.
- Follow Maven Failsafe's default include/exclude naming conventions for
  IT classes instead of hand-rolled include/exclude patterns in the POM.
- CI: `.github/workflows/build.yml` (install + deploy to GitHub Packages
  on push to `main`) and `.github/workflows/release.yml`.

## Design conventions (distilled from PR review history)

These are the recurring, consistently-enforced points from code review on
this repo (`@kevinleturc`, `@damianujma` and others). Apply them
proactively, don't wait to be asked:

- **The SDK's job is to define the contract.** Prefer typed POJOs/records
  over `Object`, raw `CICObject`/`CICArray`, or stringly-typed fields —
  the point of the SDK is to describe what the CIC REST API actually
  returns/accepts. Don't make endpoint paths configurable "just in case";
  if a client needs a different (e.g. deprecated) endpoint, that's a sign
  the client is using the wrong contract, not that the SDK should bend.
- **Default to `record` for immutable value objects — but not always.**
  Use a `record` when a public, canonical (all-args) constructor is an
  acceptable construction path — a convenience `Builder` can still sit on
  top as long as its `build()` just forwards 1:1 to
  `new MyRecord(a, b, c, ...)`; even non-trivial validation (including
  "exactly one of X or Y" checks) belongs in the compact constructor
  (e.g. `ObjectKey`). Collection-typed record components must never be
  null — normalize `null` to `List.of()`/`Map.of()` in the compact
  constructor, and defensively copy (`List.copyOf(...)`) mutable inputs.
  If "absent" is a meaningful state distinct from "empty", model it
  explicitly rather than overloading null.
  Reach for a plain `final class` instead (with a manually written
  `equals`/`hashCode`/`toString`) when any of these apply:
  - It must `extend` another class — records can't (e.g. the `XxxPage`
    type-token classes extending `CursorPageableResponse<T>`).
  - A field is genuinely optional and you want the accessor to expose it
    as `Optional<T>` for callers — record components are never declared
    `Optional` (see above), but a class can still store the field as a
    plain nullable internally while returning `Optional.ofNullable(...)`
    from its getter.
  - Construction should be fully encapsulated behind the `Builder` —
    i.e. there is deliberately no public all-args constructor, only a
    `private`/`protected Xxx(Builder builder)` — typically because the
    field set is large and/or optional-heavy enough that a positional
    constructor would be error-prone, or because the type is only ever
    meant to be obtained via a cached singleton/static factory (e.g.
    `ActionConfig.empty()`).
  - It has **more than 5 fields** — beyond that point a flat positional
    constructor (even hidden behind a thin `Builder`) becomes hard to
    read and error-prone to maintain; prefer a plain class with a
    properly encapsulated `Builder`, or decompose into smaller
    sub-objects grouping related fields.
  - The `Builder` itself is stateful/incremental rather than a 1:1
    field-setter mirror of the components — e.g. `addClass(String)`,
    `instruction(key, value)`, or a `putProperty(key, value)` style API
    that accumulates into an internal `ArrayList`/`LinkedHashMap` across
    multiple calls (see `IngestEvent.Builder`, `ActionConfig.Builder`).
- **Prefer `Duration` and standard types** over raw nullable fields,
  bare seconds/millis longs, etc. (e.g. `uptimeSeconds` → use `Duration`).
  Do not use `Optional` as a record/field component — no record in the
  SDK does this today; model a by-contract-nullable field as a plain
  nullable field instead.
- **Mapper hierarchy, not monolith mappers.** Each nested object/value
  gets its own mapper class; a parent mapper delegates to child mappers
  and should mostly just `.map(...)` over an `Optional`/list rather than
  inlining nested parsing logic. Avoid one big `XxxMapperUtils` grab-bag.
  Centralize registration in a single `XxxMapperFactory`
  (see `AgentMapperFactory` / `NucleusMapperFactory` / `QnaMapperFactory`
  for the expected `List<Entry<Class<?>, CICMapper<?>>>` static-init
  pattern) so mappers are instantiated once.
- **Use the `CICObject`/`CICArray` typed getters** (`getStringOrThrow`,
  `getOptionalString`, `getOptionalObject`, `getOptionalArray`,
  `getObjectOrThrow`, `getArrayOrThrow`, `getLocalDate`/`putLocalDate`,
  etc.) instead of generic `getProperties`/manual null/type checks. If a
  getter you need doesn't exist yet, add it to `CICObject`/`CICArray`
  rather than reimplementing the logic locally — this is a common,
  welcomed pattern across PRs.
- **Don't leak the mapper-object API into public POJOs.** Public
  SDK objects (e.g. `IngestEvent` properties) should stay as plain
  bean-shaped types; don't expose `CICObject`/`CICArray` directly on the
  public surface just because it's convenient internally.
- **Collections returned/accepted by public APIs should be unmodifiable**
  (e.g. `CICArray`/`CICObject` backing collections) so callers are forced
  through the typed `add*`/mutation API instead of mutating state
  directly.
- **Null-check constructor/builder arguments** with
  `Objects.requireNonNull(arg, "arg cannot be null")`, and validate
  semantically-invalid values (e.g. null entries in an ID list) explicitly
  rather than trusting callers.
- **Don't duplicate logic across modules.** If something in a new module
  looks like it was copy-pasted from another module's client/service
  (e.g. presigned-URL handling in `cic-ke` vs `cic-ingest`), flag it for
  factoring into a shared location (follow-up PR is fine, but call it out
  in review).
- **Exception messages matter to integrators.** Don't rewrite or augment
  a caller-provided exception message with internal detail by default —
  some callers surface that string directly (e.g. straight into an HTTP
  response). Preserve the original message/cause chain (suppressed
  exceptions) rather than discarding error context.
- **No customer names or confidential references** anywhere in the repo
  (code, tests, fixtures, README) — this repo is public. Use generic
  placeholders instead (e.g. `pretrained-model-a`).
- **Binary/media test fixtures go through Git LFS**, not committed
  directly — check `.gitattributes` coverage when adding new fixture
  types, and double-check there's no privacy/licensing concern with any
  fixture content before adding it.
- **Prefer braces on every `if`/`else`/loop body**, even single-line ones.
- **Test classes and test methods must be `public`.**
- **Keep Javadoc accurate**, including `@since` tags matching the actual
  next release version (this repo publishes no GitHub Releases — read
  the latest tag with `git tag --sort=-v:refname | head -1`, and don't
  assume the current `-SNAPSHOT` version number is it).
- When a mapper or object design choice isn't obvious from the OpenAPI
  spec, **verify against the spec** (or staging) rather than guessing,
  and note in the PR/commit what was confirmed and how.

## Backward compatibility

The project follows semantic versioning (`MAJOR.MINOR.PATCH`). The
public API (anything publicly accessible: classes, interfaces, methods,
constructors, fields, enum constants, record components, etc. not marked
internal/SPI-only) **must not break between patch or minor releases**.
A breaking change is only allowed in the next **major** release, and only
after going through a deprecation cycle:

- To remove/change public API, first mark it `@Deprecated(since = "x.y.z",
  forRemoval = true)` and keep it fully functional (delegating to the new
  API internally if needed), released in a minor/patch version.
- Document the deprecation and the replacement in the relevant
  `README.md` section (see the `PropertyArray` entry for the expected
  format) and in the Javadoc (`@deprecated` tag pointing to the
  replacement).
- Only remove the deprecated API in the next major version.
- This applies to source and binary compatibility alike, including
  changes that are easy to overlook:
  - Turning a `class` into a `record` (or vice versa) — this changes the
    type hierarchy (a `record` is implicitly `final` and extends
    `Record`), can alter `equals`/`hashCode`/`toString` semantics, and
    changes accessor method names (`getFoo()` vs `foo()`).
  - Removing or reordering record components — this changes the
    canonical constructor signature. Adding a new component is allowed
    without a major bump only if an overload constructor matching the
    previous signature is kept (delegating to the new canonical
    constructor with a sensible default for the added component).
  - Adding a new method to a public `interface` — this breaks existing
    implementers unless given a `default` implementation.
  - Changing a method's return type, a parameter type, or widening an
    exception, even if the change looks "more correct".
  - Changing the semantics of a getter (e.g. switching what counts as
    "empty" vs `null`) without a version bump and changelog entry.
  - Renaming a public class, method, or field.
- When unsure whether a change is breaking, treat it as breaking and
  follow the deprecation cycle above.

## Commit / PR conventions

- Reference the driving Jira ticket in the PR title, e.g.
  `NXENG-724: add CICBlob.builder(Path) for file-backed blobs`.
- See `CONTRIBUTING.md` for contributor requirements, including the CLA,
  build/format commands, and PR expectations.
