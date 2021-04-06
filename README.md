Super easy kotlin script to native using graalvm(convert) and upx(shrink)

kotlinc -script script2native.kts <yourscript.kts> [options] (sep: generate a bash file instead running it for you)

build native executable for script2native.kts

On Windows platform it will require Native Tool cmd, simply install Visual Studio and search "x64(or 32) Native Tools Command
Prompt for VS 2019"

```
kotlinc -script script2native.kts script2native.kts
```

If you have external library, put in "lib" folder in same directory.

Too many libs? Put following in your build.gradle.kts

```kotlin
val download: Configuration by configurations.creating

tasks.create<Copy>("downloadLibs") {
    from(download)
    into("libs")
}

dependencies {
    //... your other libraries
    //simply replace implementation("group:id:V") to download("group:id:V")
    implementation(download)
}
```

groovy script should be almost same.