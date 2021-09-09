/*
 * Copyright 2021, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

package io.spine.tools.comparables

import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterizedTypeName
import io.spine.protodata.Field.CardinalityCase.SINGLE
import io.spine.protodata.codegen.java.ClassName
import io.spine.protodata.codegen.java.Expression
import io.spine.protodata.codegen.java.JavaRenderer
import io.spine.protodata.codegen.java.Literal
import io.spine.protodata.codegen.java.MessageReference
import io.spine.protodata.codegen.java.MethodCall
import io.spine.protodata.codegen.java.TypedInsertionPoint.CLASS_SCOPE
import io.spine.protodata.codegen.java.TypedInsertionPoint.MESSAGE_IMPLEMENTS
import io.spine.protodata.renderer.SourceSet
import io.spine.protodata.select
import java.util.*
import javax.lang.model.element.Modifier.FINAL
import javax.lang.model.element.Modifier.PRIVATE
import javax.lang.model.element.Modifier.PUBLIC
import javax.lang.model.element.Modifier.STATIC
import com.squareup.javapoet.ClassName as PoetClassName

@Suppress("unused") // Used by ProtoData reflectively.
public class ComparablesJavaRenderer : JavaRenderer() {

    override fun render(sources: SourceSet) {
        val comparableTypes = select<ComparableType>().all()
        comparableTypes.forEach { type ->
            val javaClassName = classNameOf(type.name, type.declaredIn)
            val canonicalName = javaClassName.canonical
            val simpleNameIndex = canonicalName.lastIndexOf(".")
            val packageName = canonicalName.substring(0, simpleNameIndex)
            val simpleName = canonicalName.substring(simpleNameIndex + 1, canonicalName.length)
            val selfName = PoetClassName.get(packageName, simpleName)
            val javaFile = sources.file(javaFileOf(type.name, type.declaredIn))
            javaFile
                .at(MESSAGE_IMPLEMENTS.forType(type.name))
                .add("${Comparable::class.java.canonicalName}<${simpleName}>,")
            javaFile
                .at(CLASS_SCOPE.forType(type.name))
                .withExtraIndentation(level = 1)
                .add(comparison(selfName, type))
        }
    }

    private fun comparison(selfName: PoetClassName, type: ComparableType): List<String> {
        val other = Literal("other")
        val naturalOrder = Literal("naturalOrder")
        val body = CodeBlock
            .builder()
            .addStatement("\$T.requireNonNull(\$L)", Objects::class.java, other)
            .addStatement("return \$L.compare(this, \$L)", naturalOrder, other)
            .build()
        val method = MethodSpec
            .methodBuilder("compareTo")
            .addAnnotation(Override::class.java)
            .addModifiers(PUBLIC)
            .returns(Int::class.javaPrimitiveType)
            .addParameter(selfName, other.toCode())
            .addCode(body)
            .build()
        val comparatorType = ParameterizedTypeName.get(
            PoetClassName.get(Comparator::class.java),
            selfName
        )
        val comparatorProperty = FieldSpec
            .builder(comparatorType, naturalOrder.toCode(), PRIVATE, STATIC, FINAL)
            .initializer(buildComparator(type))
            .addAnnotation(AnnotationSpec.builder(SuppressWarnings::class.java)
                .addMember("value", "\$S", "Convert2MethodRef")
                .build())
            .build()
        return comparatorProperty.lines() + method.lines()
    }

    private fun buildComparator(type: ComparableType): String {
        val fields = type.fieldList
        check(fields.isNotEmpty()) { "`(compare_by)` must specify at least one field." }
        val lambdaParam = "m"
        val fieldExtractors = fields.map {
            var chain: Expression = MessageReference(lambdaParam)
            it.fieldNameList.forEach { fieldName ->
                chain = MessageReference(chain.toCode()).field(fieldName, SINGLE).getter
            }
            chain
        }.map {
            "(${type.name.simpleName} $lambdaParam) -> $it"
        }.map { Literal(it) }
        var expression = MethodCall(ClassName(Comparator::class.java), "comparing", arguments = listOf(fieldExtractors.first()))
        fieldExtractors.subList(1, fieldExtractors.size).forEach {
            expression = expression.chain("thenComparing", arguments = listOf(it))
        }
        return expression.toCode()
    }
}

private fun FieldSpec.lines() = toString().split(System.lineSeparator())

private fun MethodSpec.lines() = toString().split(System.lineSeparator())
