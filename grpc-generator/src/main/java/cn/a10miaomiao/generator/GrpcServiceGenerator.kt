package cn.a10miaomiao.generator

import pbandk.gen.ServiceGenerator
import java.nio.file.Paths

class GrpcServiceGenerator : ServiceGenerator {
    override fun generate(service: ServiceGenerator.Service): List<ServiceGenerator.Result> {
        service.debug { "Generating code for service ${service.name}" }
        val methods = service.methods.map { method ->
            val reqType = service.kotlinTypeMappings[method.inputType!!]!!
            val respType = service.kotlinTypeMappings[method.outputType!!]!!
            val methodName = method.name.replaceFirstChar {
                it.lowercase()
            }
            val packageName = service.file.packageName
            val fullName = "${packageName}.${service.name}/${method.name}"
            """
    @JvmStatic
    fun ${methodName}(
        req: $reqType,
    ) = GRPCMethod<
        $reqType, 
        $respType
    >(
        "$fullName",
        req,
        $respType.Companion
    )
            """.trimIndent()
        }
        return listOf(ServiceGenerator.Result(
            otherFilePath = Paths.get(service.filePath).resolveSibling(service.name + "GRPC.kt").toString(),
            code = """
package ${service.file.kotlinPackageName}

import com.a10miaomiao.bilimiao.comm.network.GRPCMethod

object ${service.name}GRPC {
${methods.joinToString("\n\n")}
}
            """.trimIndent()
        ))
    }
}