# JSpz

Java loader for SPZ

---

Small portions of the code have been ported from C++
to Java from https://github.com/nianticlabs/spz

---

## Change log

### Version 0.0.2

- Added support for SPZ version 3, as introduced in the SPZ library version 2.0.0.
  - The `SpzReaders` and `SpzWriters` classes now offer a method called
    `createDefault` that allows reading and writing SPZ data with the latest
    version that is supported by the library. Clients can still use the 
    `createDefaultV2` and `createDefaultV3` methods, but will usually
    use the `createDefault` method for the latest version.

### Version 0.0.1

- Initial release