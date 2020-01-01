# Kotlin blockchain

An example blockchain in Kotlin. &#x1F337;&#x1F337;&#x1F337;

<a href="../LICENSE.md">
<img src="https://unlicense.org/pd-icon.png" alt="Public Domain" align="right"/>
</a>

This software is in the Public Domain.  Please see [LICENSE.md](../LICENSE.md).

[![License](https://img.shields.io/badge/license-PD-blue.svg)](http://unlicense.org)

## Getting started

```
$ ./run.sh
```

## Notes

* This example does not support varying the difficulty (proof of work)
per-block: it is a property of the chain, not block construction.
* It would be straight-forward to replace difficulty (count of leading zeros
in the hash) with a minimum time spent working.

## Questions

* What should happen after dropping a hash function when someone adds a block
using that dropped function?
* Current code assumes the genesis has ("0") when there is no previous hash,
and not just for the genesis block.  Is this best?
* Are "holes" permitted?  For example, the chain sees new hash function
"SuperHash" appear, then another block comes without "SuperHash" (say the 2nd
caller hasn't started using "SuperHash" yet).  This would presently reset
previous with the genesis hash.  How to deal with this?

## See also

See [Awesome Blockchains](https://github.com/openblockchains/awesome-blockchains)
