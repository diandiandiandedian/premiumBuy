package com.example.myapplication.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

// 定义你的数据模型（例如 TokenTransfer）
data class TokenTransfer(
    val id: Long,
    val mode: String,
    val blockNumber: Long,
    val transactionHash: String,
    val fromAddress: String,
    val toAddress: String,
    val tokenAddress: String,
    val tokenSymbol: String,
    val value: String,
    val timestamp: String?,
    val status: String?,
    val lotteryNumbers: String?,
    val lotteryPeriod: String?,
    val createdAt: String?,
    val updatedAt: String?
)

// 创建请求体数据模型
data class TokenTransferRequest(
    val mode: String,
    val value: String,
    val toAddress: String,
    val tokenAddress: String
)

data class AssignLotteryRequest(
    val mode: String,
    val value: String,
    var toAddress: String,
    var tokenAddress: String,
    var lotteryNumber: String,
    var lotteryPeriod: String,
)

data class AssignLotteryResponse(
    val success: Boolean,
    val message: String
)


// 创建包含 `data` 数组的响应模型
data class ApiResponse(
    val success: Boolean,
    val message: String?,
    val data: List<TokenTransfer>
)

data class UrlResponse(
    val success: Boolean,
    val message: String,
    val data: UrlData
)

data class UrlData(
    val url: String
)

data class NotaryRequest(
    val itemName: List<String>,
    val itemQuantity: List<Int>,
    val itemPrice: List<Double>,
    val tokenAddress: String,
    val purchaser: String,
    val lotteryNumber: List<Int>
)

interface ApiService {
    @GET("lottery/transfer") // 改为 GET 请求
    suspend fun getTokenTransfers(
        @Query("mode") mode: String,
        @Query("value") value: String,
        @Query("toAddress") toAddress: String,
        @Query("tokenAddress") tokenAddress: String
    ): ApiResponse

    @POST("lottery/assign")
    suspend fun assignLottery(
        @Body request: AssignLotteryRequest
    ): AssignLotteryResponse

    @POST("notary/create")
    suspend fun createNotary(@Body requestBody: NotaryRequest): UrlResponse
}