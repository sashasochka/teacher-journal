# Journal for KPI teachers

Prerequisites
-------------
* sbt 0.13.0 or above
* Android SDK

Build
-----
You can build this project using sbt:

    $ sbt android:package

This will compile the project and generate an APK.

Before building you'd probably need to setup your `$ANDROID_HOME` environment variables.

On UNIX-like OSes you can do that by adding `export ANDROID_HOME="path/to/sdk/"` in `~/.bashrc`

If this fails try to remove ~/.sbt and ~/.ivy2 directories

    $ rm -rf ~/.sbt
    $ rm -rf ~/.ivy2

You'll also possibly need to setup ANDROID_HOME environment variable as a path to your android sdk directory

For more command, refer to [android-sdk-plugin for sbt](https://github.com/pfn/android-sdk-plugin).

Tips for faster development iteration
-------------------------------------
In sbt, `~` is a prefix that repeatedly runs the command when the source code is modified.

    ~ android:run

This sbt command schedules to execute compile-package-deploy-run process after you save the edited source code.
Compiling and packaging runs incrementally, so this iteration takes about only few seconds.

If you use default AVD, try genymotion or other faster virtual device. Deploying apk to the device becomes much faster!

Using IntelliJ IDEA
-------------------

    $ sbt gen-idea

Three more steps are needed for IDEA:

 * Install Scala and sbt (optionally) plugins (if not yet)
 * Project Structure -> Project -> in Project SDK section, select proper Android SDK
 * Project Structure -> Modules -> add Android facet to your project module

We do not recommend to use IDEA's own Android build system, because proguard settings are complicated and not fast.
Use commands from [android-sdk-plugin for sbt](https://github.com/pfn/android-sdk-plugin).
It runs simple and fast.

Troubleshooting
---------------

### Runtime error: `java.lang.NoSuchMethodError`, `java.lang.ClassDefNotFoundError`

The most likely cause of this error is the problem with proguard-cache. You can try:

 - remove `target` directory (and optionally `bin`, `gen`, `project/target`, `project/project`) directories
 - *OR / AND*
 - remove or comment out the `ProguardCache` settings from `build.sbt`

Then, rebuild the project, and the problem should be gone

### Build error `Android SDK build-tools not available`
[The most likely cause of this error is that your SDK build-tools are old.](https://github.com/pfn/android-sdk-plugin/issues/13) Update the Android SDK and retry.

Further Reading
---------------
- [Scaloid](https://github.com/pocorall/scaloid)
- [Scaloid APIdemos](https://github.com/pocorall/scaloid-apidemos)
- [Android SDK plugin for sbt](https://github.com/pfn/android-sdk-plugin)

