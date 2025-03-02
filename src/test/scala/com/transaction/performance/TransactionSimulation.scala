package com.transaction.performance

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._
import java.util.UUID

class TransactionSimulation extends Simulation {
  val httpProtocol = http
    .baseUrl("http://localhost:8080") // 被测服务地址
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")
    .shareConnections // 启用连接复用

  // 定义请求模板
  val createTransactionBody =
    """{"id":"${id}", "amount":100, "type":"DEPOSIT", "category":"Salary"}"""

  val headers = Map("Content-Type" -> "application/json")

  // 场景1：高并发创建交易
  val createScn = scenario("Create Transaction Stress Test")
    .feed(Iterator.continually(Map("id" -> UUID.randomUUID.toString))) // 生成唯一ID
    .exec(
      http("Create Transaction")
        .post("/api/transactions")
        .headers(headers)
        .body(StringBody(createTransactionBody))
        .check(status.is(200))
    )

  // 场景2： 查询
  val mixedScn = scenario("Query List Test")
    .feed(Iterator.continually(Map("id" -> UUID.randomUUID.toString)))
    .exec(
      http("List Transactions")
        .get("/api/transactions?pageNumber=0&pageSize=5")
        .check(status.is(200))
    )

  // 场景3：删除交易负载
  val deleteScn = scenario("Delete Transaction Stress Test")
    .feed(Iterator.continually(Map("id" -> UUID.randomUUID.toString)))
    .exec(
      http("Delete Transaction")
        .delete("/api/transactions/${id}")
        .check(status.in(204, 404)) // 允许交易不存在
    )

  // 定义负载模型
  setUp(
    // 场景1：30秒内逐步增加至100用户
    createScn.inject(
      rampUsersPerSec(10) to 100 during (30.seconds)
    ),
    // 场景2：持续5分钟稳定负载
    mixedScn.inject(
      constantUsersPerSec(50) during (5.minutes)
    ),
    // 场景3：瞬时峰值（200用户）
    deleteScn.inject(
      atOnceUsers(200)
    )
  ).protocols(httpProtocol)
   .assertions(
     global.responseTime.max.lt(1500), // 最大响应时间 <1.5秒
     global.successfulRequests.percent.gt(95) // 成功率 >95%
   )
}