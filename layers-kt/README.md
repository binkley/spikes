# Layers KT

_An experiment in style and technique in Kotlin_.

(See [Layers Kotlin](https://github.com/binkley/layers-kt) for a previous
pass at this experiment.)

This software is in the Public Domain.  Please see [LICENSE.md](LICENSE.md).

[![License](https://img.shields.io/badge/license-PD-blue.svg)](http://unlicense.org)
[![Issues](https://img.shields.io/github/issues/binkley/spikes.svg)](https://github.com/binkley/spikes/issues)

## Target architecture

* Rule-based engine
* Text-based serialization, including rules
* History with do/undo/redo and branching
* Immutable layers for presentation; mutable for editing
* Stateless but caching service
  - Suitable for horizontal scaling
* Persistent operations
  - Automatic restore of cached state on restarts
* Git persistence
* JSON REST clients
* WebSocket with STOMP for publishing state visible changes as JSON
