import org.objectweb.asm.*
import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.tree.*
import java.io.File
import java.util.zip.*

println("Script to native")
val fileName = args[0]
println("Script: $fileName")

val shortName = fileName.replace(".kts", "")

val lib = File("lib")
val libCP =
    if (!lib.exists()) null
    else if (lib.listFiles() == null) null
    else if (lib.listFiles()!!.isEmpty()) null
    else lib.listFiles()!!.filter { it.name.endsWith(".jar") }.joinToString(";") { it.absolutePath }

val fullCP = if (libCP != null) arrayOf("-cp", "\"$libCP\"") else arrayOf()

val runtime = Runtime.getRuntime()!!

println("Compiling...")
exec("kotlinc.bat", fileName, "-include-runtime", *fullCP, "-d", "$shortName.jar")
println("Finished Compile to jar")

val mainClassName = fileName.replace(".kts", "").capitalize().replace(" ", "_")
val outFile = File("$shortName.jar")

val manifestAttributes = mapOf(
    "Manifest-Version" to "1.0",
    "Created-By" to "JetBrains Kotlin",
    "Main-Class" to mainClassName
)

val zip = ZipFile(outFile)

with(ZipOutputStream(File("$shortName-all.jar").outputStream())) {
    zip.entries().iterator().forEach {
        if (it != null && it.name != "META-INF/MANIFEST.MF") {
            if (it.name == "$mainClassName.class") {
                println("Editing Main Class")
                val classReader = ClassReader(zip.getInputStream(it).readBytes())
                val classNode = ClassNode()
                classReader.accept(classNode, ClassReader.EXPAND_FRAMES)

                classNode.methods
                    .find { m -> m.name == "main" && m.desc == "([Ljava/lang/String;)V" }!!
                    .apply {
                        instructions.clear()
                        visitTypeInsn(NEW, mainClassName)
                        visitInsn(DUP)
                        visitIntInsn(ALOAD, 0)
                        visitMethodInsn(INVOKESPECIAL, mainClassName, "<init>", "([Ljava/lang/String;)V", false)
                        visitInsn(RETURN)
                        visitEnd()
                    }



                val writer = ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
                classNode.accept(writer)
                putNextEntry(ZipEntry(it.name))
                write(writer.toByteArray())
                closeEntry()
                println("Finished editing Main Class")
            } else {
                putNextEntry(it)
                write(zip.getInputStream(it).readBytes())
                closeEntry()
            }
        }
    }

    putNextEntry(ZipEntry("META-INF/MANIFEST.MF"))
    write((manifestAttributes.map { "${it.key}: ${it.value}" }.joinToString("\r\n") + "\n").toByteArray(Charsets.UTF_8))
    closeEntry()

    close()
    println("Full jar finished")
}

@Suppress("IMPLICIT_CAST_TO_ANY")
if (args.contains("sep")) {
    println("Please run runme.bat/.sh, If you are in windows make sure open Native Tool CMD")
    with(File("runme${if (System.getProperty("os.name").toLowerCase().contains("win")) ".bat" else ".sh"}")) {
        writeText(listOf(
            "native-image.cmd",
            *fullCP,
            "-jar",
            "$shortName-all.jar",
            "--no-fallback").joinToString(" ") + "&&" + "upx -9 $shortName-all.exe")
    }
} else {
    println("Converting to native...")
    exec("native-image.cmd",
        *fullCP,
        "-jar",
        "$shortName-all.jar",
        "--no-fallback")
    println("UPX shirking...")
    exec("upx", "-9", "$shortName-all.exe")
}

fun exec(vararg cmd: String) {
    val reader = runtime.exec(cmd).inputStream.reader().buffered()
    while (true) {
        val l: String = reader.readLine() ?: break
        println(l)
    }
}
