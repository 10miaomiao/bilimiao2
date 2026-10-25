package cn.a10miaomiao.bilimiao.danmaku.renderer

import cn.a10miaomiao.bilimiao.danmaku.model.IDanmakus
import cn.a10miaomiao.bilimiao.danmaku.model.IDisplayer

/**
 * 弹幕渲染器接口
 */
interface IRenderer {

    fun draw(disp: IDisplayer, danmakus: IDanmakus, renderingState: RenderingState)

    fun clear()

    fun clearRetainer()

    fun release()

    fun setVerifierEnabled(enabled: Boolean)

    fun alignBottom(enable: Boolean)
}
