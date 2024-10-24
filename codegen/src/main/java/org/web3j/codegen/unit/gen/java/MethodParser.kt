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

import com.squareup.javapoet.ParameterSpec
import org.junit.jupiter.api.BeforeAll
import java.lang.reflect.Method
import java.util.Arrays
import java.util.stream.Collectors
import java.util.stream.Stream
import javax.lang.model.element.Modifier

/*Class that when given a method provides a JavaPoet method spec. */
class MethodParser(
    private val method: Method,
    private val theContract: Class<*>,
    private val uniqueMethodName: String
) {
    val methodSpec: MethodSpec
        get() = if (methodNeedsInjection()
        ) MethodSpecGenerator(
            uniqueMethodName,
            BeforeAll::class.java,
            Modifier.STATIC,
            defaultParameterSpecsForEachUnitTest(),
            generateStatementBody()
        )
            .generate()
        else MethodSpecGenerator(uniqueMethodName, generateStatementBody()).generate()

    private fun methodNeedsInjection(): Boolean {
        return Arrays.asList<Class<*>>(*method.parameterTypes)
            .containsAll(
                Arrays.asList<Class<out Any>>(
                    Web3j::class.java,
                    TransactionManager::class.java,
                    ContractGasProvider::class.java
                )
            )
    }

    private fun defaultParameterSpecsForEachUnitTest(): List<ParameterSpec> {
        return Stream.of<ParameterSpec>(
            ParameterSpec.builder(Web3j::class.java, NameUtils.toCamelCase(Web3j::class.java))
                .build(),
            ParameterSpec.builder(
                TransactionManager::class.java,
                NameUtils.toCamelCase(TransactionManager::class.java)
            )
                .build(),
            ParameterSpec.builder(
                ContractGasProvider::class.java,
                NameUtils.toCamelCase(ContractGasProvider::class.java)
            )
                .build()
        )
            .collect(Collectors.toList<ParameterSpec>())
    }

    private fun generateStatementBody(): Map<String, Array<Any>> {
        val methodBodySpecification: MutableMap<String, Array<Any>> = LinkedHashMap()
        val parser = JavaParser(theContract, method, JavaMappingHelper())
        val javaPoetStringTypes = parser.generatePoetStringTypes()
        val genericParameters = parser.generatePlaceholderValues()
        methodBodySpecification[javaPoetStringTypes] = genericParameters
        if (methodNeedsAssertion()) {
            val assertionJavaPoet = parser.generateAssertionJavaPoetStringTypes()
            val assertionParams = parser.generateAssertionPlaceholderValues()
            methodBodySpecification[assertionJavaPoet] = assertionParams
        }
        return methodBodySpecification
    }

    private fun methodNeedsAssertion(): Boolean {
        return !methodNeedsInjection()
    }
}
