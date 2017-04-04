##General
The module contains the custom Javadoc doclet, which excludes
elements annotated with `org.spine3.Internal`.  

##Usage
To use the doclet, specify the Javadoc options:

`javadoc -doclet org.spine3.tools.javadoc.ExcludeInternalDoclet -docletpath "classpathlist" ...`

##Tests
For the tests, we use specially prepared sources, that cover all test cases.

The sources are located in `resources` folder.
