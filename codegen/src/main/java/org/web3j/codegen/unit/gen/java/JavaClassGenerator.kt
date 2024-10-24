/*
 * Copyright 2019 Web3 Labs Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.web3j.codegen.unit.gen.java

import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.TypeSpec
import org.web3j.codegen.unit.gen.MethodFilter
import org.web3j.commons.JavaVersion
import java.io.File
import java.io.IOException
import javax.lang.model.element.Modifier

/**
 * Class that generates the unit tests classes for the contracts. The class writes to
 * src/test/java/contracts and each file is named after the contract + "Test" e.g GreeterTest
 */
class JavaClassGenerator(
    private val theContract: Class<*>,
    private val packageName: String,
    private val writePath: String
) : UnitClazssGenerator {
    @Throws(IOException::class)
    override fun writeClass() {
        val EVM_ANNOTATION = ClassName.get("org.web3j", "EVMTest")
        val annotationSpec = AnnotationSpec.builder(EVM_ANNOTATION)
        if (JavaVersion.getJavaVersionAsDouble() < 11) {
            val GethContainer = ClassName.get("org.web3j", "NodeType")
            annotationSpec.addMember("value", "type = \$T.GETH", GethContainer)
        }
        val testClass =
            TypeSpec.classBuilder(theContract.simpleName + "Test")
                .addMethods(
                    MethodFilter.generateMethodSpecsForEachTest(
                        theContract
                    )
                )
                .addAnnotation(annotationSpec.build())
                .addField(
                    theContract,
                    NameUtils.toCamelCase(theContract),
                    Modifier.PRIVATE,
                    Modifier.STATIC
                )
                .build()
        val javaFile: JavaFile = JavaFile.builder(packageName, testClass).build()
        javaFile.writeTo(File(writePath))
    }
}
