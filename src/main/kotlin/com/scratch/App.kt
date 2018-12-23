package com.scratch

import com.scratch.config.AppConfig
import com.scratch.controller.MainController
import com.zandero.rest.RestRouter
import io.vertx.core.Vertx

fun main(args: Array<String>) {
    val vertx = Vertx.vertx()
    val rest = MainController()
    val router = RestRouter.register(vertx, rest)

    vertx.createHttpServer()
        .requestHandler(router::accept)
        .listen(AppConfig.APP_PORT)
}
