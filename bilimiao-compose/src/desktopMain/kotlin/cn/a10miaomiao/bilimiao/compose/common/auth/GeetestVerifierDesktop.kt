package cn.a10miaomiao.bilimiao.compose.common.auth

/**
 * Desktop 端 Geetest 验证器占位实现
 * Desktop 端不支持 Geetest 验证码
 */
class GeetestVerifierDesktop : GeetestVerifier {
    override fun startVerification(callback: GeetestCallback) {
        // Desktop 端不支持 Geetest 验证码，直接回调失败
        println("Geetest verification is not supported on Desktop")
    }
}
