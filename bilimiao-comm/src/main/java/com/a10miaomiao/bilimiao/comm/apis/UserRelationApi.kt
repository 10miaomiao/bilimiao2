package com.a10miaomiao.bilimiao.comm.apis

import com.a10miaomiao.bilimiao.comm.network.ApiHelper
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp

class UserRelationApi {

    /**
     * 关注Up主
     */
    fun modify(
        mid: String,
        mode: Int, // 1为关注，2为取消关注
    ) = MiaoHttp.request {
        url = BiliApiService.biliApi("x/relation/modify")
        formBody = ApiHelper.createParams(
            "fid" to mid,
            "act" to mode.toString(),
//            "re_src" to "32",
        )
        method = MiaoHttp.POST
    }

    /**
     * 关注的up主
     */
    fun followings(
        mid: String,
        pageNum: Int = 1,
        pageSize: Int = 50,
        order: String = "attention" // 最常访问排列：attention，关注顺序排列：留空
    ) = MiaoHttp.request {
        url = BiliApiService.biliApi(
            "x/relation/followings",
            "vmid" to mid,
            "pn" to pageNum.toString(),
            "ps" to pageSize.toString(),
            "order_type" to order,
            "order" to "desc",
        )
    }

    /**
     * 搜索关注的up主
     */
    fun search(
        mid: String,
        name: String,
        pageNum: Int = 1,
        pageSize: Int = 50,
    ) = MiaoHttp.request {
        url = BiliApiService.biliApi(
            "x/relation/followings/search",
            "vmid" to mid,
            "name" to name,
            "pn" to pageNum.toString(),
            "ps" to pageSize.toString(),
        )
    }

    /**
     * 关注分组
     */
    fun tags() = MiaoHttp.request {
        url = BiliApiService.biliApi("x/relation/tags")
    }

    /**
     * 分组详情
     */
    fun tagDetail(
        tagId: Int, // 特别关注恒为-10,默认分组恒为0
        order: String = "attention", // 最常访问排列：attention，关注顺序排列：留空
        pageNum: Int = 1,
        pageSize: Int = 50,
    ) = MiaoHttp.request {
        url = BiliApiService.biliApi("x/relation/tag",
            "tagid" to tagId.toString(),
            "pn" to pageNum.toString(),
            "ps" to pageSize.toString(),
            "order_type" to order,
            "order" to "desc",
        )
    }

    /**
     * 创建分组
     */
    fun tagCreate(
        tag: String, // 分组名
    ) = MiaoHttp.request {
        url = BiliApiService.biliApi("x/relation/tag/create")
        formBody = ApiHelper.createParams(
            "tag" to tag,
        )
        method = MiaoHttp.POST
    }

    /**
     * 修改分组
     */
    fun tagUpdate(
        tagId: Int,
        tagName: String, // 分组名
    ) = MiaoHttp.request {
        url = BiliApiService.biliApi("x/relation/tag/update")
        formBody = ApiHelper.createParams(
            "tagid" to tagId.toString(),
            "name" to tagName,
        )
        method = MiaoHttp.POST
    }

    /**
     * 删除分组
     */
    fun tagDelete(
        tagId: Int,
    ) = MiaoHttp.request {
        url = BiliApiService.biliApi("x/relation/tag/del")
        formBody = ApiHelper.createParams(
            "tagid" to tagId.toString(),
        )
        method = MiaoHttp.POST
    }

    /**
     * 批量复制关注到分组
     */
    fun copyUsers(
        fids: List<String>, // 待复制的用户 mid 列表
        tagids: List<Int>, // 目标分组 id 列表
    ) = MiaoHttp.request {
        url = BiliApiService.biliApi("x/relation/tags/copyUsers")
        formBody = ApiHelper.createParams(
            "fids" to fids.joinToString(","),
            "tagids" to tagids.joinToString(","),
        )
        method = MiaoHttp.POST
    }

    /**
     * 批量移动关注到分组
     */
    fun moveUsers(
        fids: List<String>, // 待移动的用户 mid 列表
        beforeTagids: List<Int>, // 原分组 id 列表
        afterTagids: List<Int>, // 新分组 id 列表
    ) = MiaoHttp.request {
        url = BiliApiService.biliApi("x/relation/tags/moveUsers")
        formBody = ApiHelper.createParams(
            "fids" to fids.joinToString(","),
            "beforeTagids" to beforeTagids.joinToString(","),
            "afterTagids" to afterTagids.joinToString(","),
        )
        method = MiaoHttp.POST
    }

    fun addUsers(
        fids: List<String>, // 用户 mid 列表
        tagIds: List<Int>, // 分组 id 列表
    ) = MiaoHttp.request {
        url = BiliApiService.biliApi("x/relation/tags/addUsers")
        formBody = ApiHelper.createParams(
            "fids" to fids.joinToString(","),
            "tagids" to tagIds.joinToString(","),
        )
        method = MiaoHttp.POST
    }

    /**
     * 批量查询用户与自己关系
     */
    fun interrelations(
        fids: List<String>,
    ) = MiaoHttp.request {
        url = BiliApiService.biliApi("x/relation/interrelations",
            "fids" to fids.joinToString(",")
        )
    }

}