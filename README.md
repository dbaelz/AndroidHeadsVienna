## About
This project shows how to migrate Gradle Build Scripts written in Groovy
to the `Gradle Kotlin DSL`. The necessary steps are done in separate commits
and explained in this README.

Also, it was the live demo part of my talk
at the [AndroidHeads Vienna](https://www.meetup.com/de-DE/AndroidHeads/events/hndkrqyzgbgb/).
The [slides](Gradle_Kotlin_DSL.pdf) contain some links to additional
resources that are a good starting point for the Kotlin DSL and some
other Gradle topics mentioned in this file.


## Migration Steps
Initially the build scripts of the project have the well-known structure of
Gradle: In the root directory the build.gradle and the settings.gradle.
In the app module another build.gradle for the app project.

The project was generated with a template by Android Studio.
To show some typical cases, three changes are made in the `app/build.gradle` script:
* The static method `generateVersionCode()` was added and is called in the defaultConfig.
It calculates the version code of the Android app based on the Git commit count.
* An (unused) BuildConfig field `demoApiToken` is defined in the build script. The value of this
field is provided by a [Gradle property](gradle.properties).
* Additional, the `espresso-core` dependency defines an `exclude` of a (non-existing) package.
It has no effect in this project, but it's exemplary for exclude definition in dependencies.


The approach for the migration of these three scripts is quite simple:
* Change the existing Groovy scripts so the syntax is more like that of Kotlin.
* Migrate the build scripts to Kotlin, refresh them and check for errors.
* Fix these errors until it's running/building again.
* Bonus: Revise the existing scripts to follow Gradle and Kotlin best practices.
 

### [Step 1: Applying Plugins](https://github.com/dbaelz/AndroidHeadsVienna/commit/076ef0f6bc14ae3d7ab9b2563199cd1fff71e955)
Instead of the legacy `apply()` function use the `plugins {}` block to
apply plugins and define them with the `id("package")` function.
The `apply()` function still works with the Kotlin DSL, but only the `plugins {}`
block provides advanced features like type-safe accessors for extensions/configurations.

### [Step 2: Quotes](https://github.com/dbaelz/AndroidHeadsVienna/commit/9112ea4dcd9a39da56969133e1a629229d8a3097)
In Groovy single and double quoted Strings are used, in Kotlin only double quotes
are supported. That's something we need to tackle.
Even if there are [subtle differences](http://groovy-lang.org/syntax.html#_string_summary_table),
for the migration it's sufficient to change all single quotes into double quotes.

### [Step 3: Assignments and Function/Method Calls](https://github.com/dbaelz/AndroidHeadsVienna/commit/9112ea4dcd9a39da56969133e1a629229d8a3097)
Another difference between Groovy and Kotlin is, that Groovy has some cases of optionality,
for example with [parentheses for method calls](http://groovy-lang.org/semantics.html#_optional_parentheses),
that aren't valid code in Kotlin. So we have to change them as well.

What at first seems as a straight forward approach, raises further questions:
Are these methods still provided by the Kotlin DSL as methods or
instead as properties? Could we use the property syntax for all provided methods?
Maybe there are Extension Functions provided by the DSL?

To answer these questions we have to take a deeper look into the documentation
and the source code of the Kotlin DSL and check every assignment and function call.
Or we could just change them to the best of knowledge (to method calls)
and fix them after the migration. That's the approach I'd like to recommended,
because it's done much faster. The linked commit shows this changes in all three
scripts. It uses method calls and property assignments based on guts and maybe
some knowledge of the Kotlin DSL.

### [Step 4: Migration](https://github.com/dbaelz/AndroidHeadsVienna/commit/91afecbd09db25322497a4caf6533d576eba5b22)
Step number 4 is the fastest and easiest in this guide. The Groovy Gradle scripts end with
`.gradle` extension, the Kotlin DSL scripts with `gradle.kts`. So we just have to rename
the three existing files and refresh the scripts.
    

### [Step 5: Fix Errors](https://github.com/dbaelz/AndroidHeadsVienna/commit/91afecbd09db25322497a4caf6533d576eba5b22)
After the renaming and refresh of the example project it won't build and show some errors.
Something that might happened with every project after the Kotlin DSL migration.
That's no problem, because in this step we will fix the errors and make sure it's a
working project again.

There are some required fixes, which are common/typical for an Android project:

#### Build Types
In Groovy configuration actions for the different build types could be added
just by name (e.g. `release`) and a closure containing the configuration block.
On runtime, the configuration is located and the action added. In a statically-typed
language such as Kotlin, we have to locate the configuration by its name with the
`getByName("release")` method and provide a lambda with the configuration.
In contrast to a Groovy script, auto-completion and
jump to the source in the lambda are working now. 

#### Assignments and Method Calls
After reloading the Kotlin build script, another error inside the `release`
configuration is shown. The `minifyEnabled` property doesn't exist. Due
the auto-completion it's easy to find out, that it must be replaced with
the function call `setMinifyEnabled(false)` or we could use the
property syntax and change it to `isMinifyEnabled = false`.

Also, the `exclude` in the espresso dependency shows an error.
The Groovy script uses a special [map property notation](http://groovy-lang.org/groovy-dev-kit.html#_map_property_notation)
to create a map and calls the `exclude` method with the map as argument.
The Kotlin DSL provides an Extension Function (also called `exclude`),
which takes the group and module as arguments, creates the map and 
calls the original exclude method.

#### generateVersionCode() Function
We change his static Groovy function to a Kotlin top-level function.
Inside the function some changes are required. The variable declaration
(`var`instead of `def`) changes, also `result.empty` and the cast
must be changed to the according Kotlin methods.
Furthermore, the function called a Groovy Extension Module
(similar to an Extension Function) `execute()` which execute the String
command in a separate process. To fix this, we rebuild the logic of the
Groovy function with the methods from the Java Standard Library
and some Extension Functions from the Kotlin Standard Library.

#### demoApiToken Gradle Property
Gradle, exactly the `Project` class as main API for the communication,
parses the property files and provides the values as variables.
In the Groovy script we could just reference the `demoApiToken` by its name and use it.
For Kotlin the variable must be declared and the initialization is delegated
to the project object.

#### kotlin_version Variable
Usually, Gradle Groovy scripts use the
[extra properties](https://docs.gradle.org/current/dsl/org.gradle.api.plugins.ExtraPropertiesExtension.html)
to define the versions in variables. Such a variable is the 
`kotlin_version` as declared in the root build script and used in the root and the
app build script. This variable declaration could be changed to a `val`,
but the variable is only accessible inside the containing script. Therefore, we just
copy the version into the dependency String. The below `Bonus` section
shows how this could be improved with some additional Gradle logic.

#### Clean Task
Clean-up of the root build directory is done with the `clean` task as
defined in the root script. In Groovy the task with the given name is generated
on runtime. As mentioned in the `Build Types` section such transformation isn't
available in Kotlin. Therefore, we have to use the method `task("clean")`
to create the task and provide a lambda for the configuration.


### Bonus
At this point, the migration of the Groovy build script to the Kotlin DSL is done.
Everything should work as before, but in addition the advantages of the Kotlin DSL
such as type-inference (and therefore auto-completion) should help to ease the
work with Gradle build scripts.

At this point we could stop the migration or make further optimizations which
increase the maintainability and reusability of some parts of the build scripts.

### [buildSrc Directory](https://github.com/dbaelz/AndroidHeadsVienna/commit/997811ce2c840a069f1b5270bddbcd4723cc528c)
The [buildSrc](https://docs.gradle.org/current/userguide/organizing_gradle_projects.html#sec:build_sources)
directory provides a way to encapsulated custom tasks, plugins and imperative logic
outside of the build script. The code inside the directory is compiled and added
to the classpath of all build scripts. It wasn't introduced with the Kotlin DSL.
Instead, it's available in Gradle for some time and could be used with Groovy as well.

In our example project, we create the project with the required build script (of course
as Kotlin build script) and a `Config.kt` file inside the source subdirectory
of buildSrc.

#### generateVersionCode() Function
As first optimization step, the `generateVersionCode()` is moved inside the Config
file. As mentioned above, the code inside the buildSrc directory is compiled and added
to the classpath of all build scripts. So the function could be referenced inside
the app build script and used as before. Unfortunately, there's an
[open issue](https://github.com/gradle/kotlin-dsl/issues/514) that prevents
Android Studio from opening the buildSrc source files when we jump into the source.
Instead, the compiled class file is opened. This is hopefully fixed soon. 

#### Kotlin Version Variable
Another use case for the buildSrc is to move the version information inside
the pre-compiled code. This improves the redundant definition of the Kotlin
version in the root and app build script.

The Config.kt now consist of a `BuildConfig` object (singleton) with the 
versions as variables. To show this on multiple occasions, several versions
e.g. the Kotlin or min/target/compile SDK are extracted.

Due an [issue in the Kotlin DSL](https://github.com/gradle/kotlin-dsl/issues/1291)
the reference in the root build script must be fully-qualified to work. In the
app build script, we could import the BuildConfig class and reference
the variables with qualifier.

### [Kotlin DSL Extension Functions](https://github.com/dbaelz/AndroidHeadsVienna/commit/9ff2362eb6624a5ed8bd6b5f88bdf7a6e9fa8c87)
As mentioned before, the Kotlin DSL provides Extension Functions to make
the work with Kotlin and Gradle easier. An example for this is
the `kotlin()` extension for the `DependencyHandler` class of Gradle. With this
extension, the definition of dependencies in the `org.jetbrains.kotlin:kotlin-`
namespace is shortened.



## License
Copyright (c) Daniel Bälz
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
  list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice,
  this list of conditions and the following disclaimer in the documentation
  and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DA