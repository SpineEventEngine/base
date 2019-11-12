/*
 * Copyright 2019, TeamDev. All rights reserved.
 *
 * Redistribution and use in source and/or binary forms, with or without
 * modification, must retain the above copyright notice and the following
 * disclaimer.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package io.spine.tools.validate

import com.squareup.javapoet.MethodSpec
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier.INTERNAL
import com.squareup.kotlinpoet.TypeSpec
import io.spine.code.java.ClassName
import io.spine.code.java.PackageName
import io.spine.code.java.SimpleClassName
import io.spine.type.MessageType
import javax.lang.model.element.Modifier.PUBLIC
import com.squareup.javapoet.ClassName as JavaClassName
import com.squareup.javapoet.CodeBlock as JavaCode
import com.squareup.kotlinpoet.ClassName as KotlinClassName

class MessageValidatorFactory(type: MessageType) {

    private val packageName: PackageName = type.javaPackage()
    private val messageClassName: ClassName = type.javaClassName()
    private val validatorClassName: SimpleClassName = messageClassName
            .toSimple()
            .with("_Validator")

    fun generateClass(): FileSpec {
        val validatedType = KotlinClassName.bestGuess(messageClassName.canonicalName())
        val validateMethod = FunSpec
                .builder("validate")
                .returns(Unit::class)
                .addAnnotation(JvmStatic::class)
                .addParameter("msg", validatedType)
                .build()
        val classSpec = TypeSpec
                .objectBuilder(validatorClassName.value())
                .addModifiers(INTERNAL)
                .addFunction(validateMethod)
                .build()
        return FileSpec.get(packageName.value(), classSpec)
    }

    fun generateValidationMethod(): String {
        val msg = "msg"
        val className = JavaClassName.bestGuess(messageClassName.toSimple().value())
        val validatorName = JavaClassName.bestGuess(validatorClassName.value())
        val body = JavaCode
                .builder()
                .addStatement("\$T \$N = build()", className, msg)
                .addStatement("\$T.validate(\$N)", validatorName, msg)
                .addStatement("return \$N", msg)
                .build()
        return MethodSpec.methodBuilder("vBuild")
                .addAnnotation(Override::class.java)
                .returns(className)
                .addCode(body)
                .addModifiers(PUBLIC)
                .build()
                .toString()
    }
}
