## General

The module contains the custom Javadoc doclet, which excludes
elements annotated with `io.spine.Internal`.

This module relies on JDK 8 API.

## Usage

To use the doclet, specify the Javadoc options:

`javadoc -doclet ExcludeInternalDoclet -docletpath "classpathlist" ...`

## Tests

For the tests, we use specially prepared sources, that cover all test cases.

The sources are located in `resources` folder.
