# Fork changelog

Changes made in this fork of
[KeenSecurityLab/BinAbsInspector](https://github.com/KeenSecurityLab/BinAbsInspector), which has
had no commits since May 2024. Upstream history and authorship are preserved and the GPL-3.0
licence is unchanged.

## Unreleased

### Fixed

- **The z3 load failure now says why it failed**
  (upstream [#77](https://github.com/KeenSecurityLab/BinAbsInspector/issues/77)).

  `checkZ3Installation()` caught `UnsatisfiedLinkError` and logged one line saying z3 could not be
  detected. With no evidence in the message, a user cannot tell a missing library from a version
  mismatch or from a library sitting in a directory the linker never searches. The reporter had
  copied the shared object into `/usr/local/bin` instead of a library directory; neither they nor
  a second reporter got past the message in over a year.

  The failure now reports the underlying linkage error and its message, the library file expected
  on the current platform, the full `java.library.path`, and the value of the platform's native
  search variable (`PATH`, `LD_LIBRARY_PATH` or `DYLD_LIBRARY_PATH`). It states that the library
  belongs in a library directory such as `/usr/local/lib` rather than `/usr/local/bin`, and that
  its version has to match the bindings in `lib/com.microsoft.z3.jar`.

  The catch clause also missed a case: once the z3 class has been initialised and its static
  loader has failed, a later attempt raises `NoClassDefFoundError` rather than
  `UnsatisfiedLinkError`, which propagated out of the check instead of being reported. Both are
  handled now.

- **Fixed the documented `docker run` command not finding the binary**
  (upstream [#83](https://github.com/KeenSecurityLab/BinAbsInspector/issues/83)).

  The README tells you to mount your working directory at `/data/workspace`, but the image left
  its working directory at `/data/workspace/BinAbsInspector` from the build stage, so a relative
  `-import` path resolved to `/data/workspace/BinAbsInspector/<file>` while the file was at
  `/data/workspace/<file>`. Ghidra then reported it as not a valid file, with nothing to suggest
  a path problem. The image now returns to `/data/workspace` after the build, and the README
  states where the binary has to live.

  The entrypoint also passed `~` as the Ghidra project path. In exec form there is no shell to
  expand it, so Ghidra created a directory literally named `~` inside the user's mounted
  directory. The project now goes to `/tmp/ghidra-project`.

  *Not verified by running the container:* Docker was not available on the machine this was
  fixed on. The cause is read off the Dockerfile and the README, and anyone with Docker should
  confirm before relying on it.

### Documentation

- **Added a Ghidra/JDK compatibility table to the README**
  (upstream [#82](https://github.com/KeenSecurityLab/BinAbsInspector/issues/82)).

  The README never stated which Ghidra or JDK version was expected. Running Ghidra 10.1.2 on
  JDK 17 or 21 makes Ghidra report `Unable to locate script class: BinAbsInspector.java`, which
  names neither the JDK nor the version constraint, so the reporter had no way to reach the cause
  from the message. The requirement belongs to Ghidra rather than to this project, so it is
  documented rather than worked around.

### Added

- `Z3DiagnosticsTest` covers the new diagnostic: that it carries the underlying error, names the
  expected library and every path searched, points at a library directory rather than `bin`, and
  survives an error with no message. The tests need no Ghidra program fixture.

### Verified build

`gradle buildExtension` and `gradle test` pass against Ghidra 10.4 with JDK 17.0.20.1 and
Gradle 7.4 on Windows, producing `dist/ghidra_10.4_PUBLIC_<date>_BinAbsInspector.zip`.

- **Added a FAQ** covering the three questions the tracker keeps receiving: whether IDA can
  replace Ghidra (no, and why, with the coupling quantified, upstream
  [#84](https://github.com/KeenSecurityLab/BinAbsInspector/issues/84)), the JDK mismatch behind
  "Unable to locate script class", and what to check when z3 fails to load.
