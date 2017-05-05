Git-SemVer Gradle Plugin
========================

When applied, Git-SemVer adds a method `gitMavenVersion()` to the target project.
 
The method `gitMavenVersion()` uses the JGit equivalent of `git describe` to determine
the git version. If the current repository version is not a tag following the
[Semantic Versioning](http://semver.org/) conventions the version is generated as an
feature increment to the previous Git tag and "-SNAPSHOT" to fit the Maven convention.

Usage
-----

Apply the plugin using standard Gradle convention:

```groovy
plugins {
  id 'eu.t-systems-mms.git-semver' version '<current version>'
}
```

Set the version of a project by calling:

```groovy
version gitMavenVersion()
```

Tasks
-----
This plugin adds a `printVersion` task, which will echo the project's configured version
to standard-out.

License
-------

This plugin is made available under the [Apache 2.0 License](http://www.apache.org/licenses/LICENSE-2.0).