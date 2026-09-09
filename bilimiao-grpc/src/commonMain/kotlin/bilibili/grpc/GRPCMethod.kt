package bilibili.grpc

/**
 * 单个 gRPC 方法调用的描述。
 *
 * 由 :bilimiao-grpc-proto 生成的 *GRPC.kt service stub 构造，供上层传输层
 * （bilimiao-comm 的 BiliGRPCHttp）发起实际的 HTTP/2-gRPC over HTTP 请求。
 *
 * @param name 形如 "package.Service/Method" 的完整方法名
 * @param reqMessage 请求消息（用于编码）
 * @param respMessageCompanion 响应消息伴生对象（用于解码）
 */
class GRPCMethod<ReqT : pbandk.Message, RespT : pbandk.Message>(
    val name: String,
    val reqMessage: ReqT,
    val respMessageCompanion: pbandk.Message.Companion<RespT>,
)
