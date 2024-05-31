package io.spine.code.proto

import io.kotest.matchers.shouldBe
import io.spine.io.replaceExtension
import io.spine.string.toBase64Encoded
import java.io.File
import java.io.InputStream
import java.nio.file.Path
import kotlin.io.path.inputStream
import kotlin.io.path.writeBytes
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

@DisplayName("`CodeGeneratorRequestWriter` should")
internal class CodeGeneratorRequestWriterSpec {

    private lateinit var requestFile: File
    private lateinit var writer: CodeGeneratorRequestWriter
    private lateinit var input: InputStream

    @BeforeEach
    fun prepareInput(@TempDir dir: Path) {
        val inputFile = dir.resolve("input.stream")
        // Request the file in the directory which does not exist.
        requestFile = dir.resolve("nested/request.binbp").toFile()
        val request = constructRequest(requestFile.absolutePath.toBase64Encoded())
        inputFile.writeBytes(request.toByteArray())
        input = inputFile.inputStream()
        writer = CodeGeneratorRequestWriter(input)
    }

    @AfterEach
    fun closeInput() {
        input.close()
    }

    @Test
    fun `write binary version of the request`() {
        writer.writeBinary()
        requestFile.exists() shouldBe true
    }

    @Test
    fun `write JSON version of the request`() {
        writer.writeJson()
        requestFile.replaceExtension("pb.json").exists() shouldBe true
    }
}
