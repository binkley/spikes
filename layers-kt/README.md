# Layers KT

_An experiment in style and technique in Kotlin_.

(See [Layers Kotlin](https://github.com/binkley/layers-kt) for a previous
pass at this experiment.)

This software is in the Public Domain.  Please see [LICENSE.md](../LICENSE.md).

[![License](https://img.shields.io/badge/license-PD-blue.svg)](http://unlicense.org)
[![Issues](https://img.shields.io/github/issues/binkley/spikes.svg)](https://github.com/binkley/spikes/issues)

**Layers-KT** is a _layered map_: A key-value dictionary that tracks edit
histories.

## Target architecture

* &#x2713; Immutable layers for presentation; mutable for editing
* &#x2713; Rule-based engine
* &#x2713; Text-based serialization, including rules, diffs, notes, etc
* &#x2713; Git persistence
  - &#x2713; History with do/undo/redo (but no API yet for undo/redo)
  - &#x231B; Branching
* &#x2713; Persistent operations
  - Automatic restore of cached state on restarts
* &#x2713; Stateless but caching service
  - Suitable for horizontal scaling
* &#x231B; JSON REST clients
* &#x231B; [WebSocket](https://en.wikipedia.org/wiki/WebSocket) with
  [STOMP](https://stomp.github.io) for publishing state visible changes as
  JSON (cf,
  [HTML Standard](https://html.spec.whatwg.org/multipage/web-sockets.html))

## Implementation

* [`Layers.kt`](./src/main/kotlin/hm/binkley/layers/Layers.kt)
  - Top-level read-only types
* [`MutableLayers.kt`](src/main/kotlin/hm/binkley/layers/PersistedMutableLayers.kt)
  - In-memory layered-map (map with history) implementation
* [`PersistedLayers.kt`](./src/main/kotlin/hm/binkley/layers/PersistedLayers.kt)
  - Persisted-in-Git layered-map (map with history) implementation
  - Persists the rules and values as Kotlin scripts
  - Also, persists for each layer: More human-friendly (`diff`-friendly)
    changesets, and optional free-form notes 
