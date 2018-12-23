package com.scratch.controller

import com.mongodb.BasicDBObject
import com.mongodb.MongoClient
import com.mongodb.MongoClientURI
import com.scratch.config.AppConfig
import com.scratch.helper.DateHelper
import com.zandero.rest.annotation.Post
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import java.lang.Exception
import javax.ws.rs.*
import javax.ws.rs.core.Context
import javax.ws.rs.core.MediaType

@Path("/")
class MainController {
    @Post
    @Path("*")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun test(@Context context: RoutingContext): JsonObject {
        val currTimestamp = System.currentTimeMillis()
        val daysInMiliseconds = DateHelper.getDaysInMiliseconds(AppConfig.NO_USER_ACT_ON_DAYS)
        val resetPeriodInMiliseconds = DateHelper.getDaysInMiliseconds(AppConfig.NO_USER_ACT_ON_PERIOD)

        var body = context.bodyAsJson
        var mongoClient: MongoClient? = null
        var requestedRecommendations = mutableListOf<JsonObject>()

        try {
            val connectionString = MongoClientURI(AppConfig.MONGO_CONNECTION_STRING)
            mongoClient = MongoClient(connectionString)
            val db = mongoClient.getDatabase(AppConfig.MONGO_DB_NAME)
            val collection = db.getCollection(AppConfig.MONGO_COLLECTION_NAME)

            // array of ids need to be removed
            var toRemoveIds = arrayOf<String>()

            // get array of requested identifiers
            val recommendations = body.getJsonArray("recommendations")
            var vendorItemIdentifiers = arrayOf<String>()
            for (i in 0..(recommendations.size() - 1)) {
                val item = recommendations.getJsonObject(i)
                requestedRecommendations.add(item)
                vendorItemIdentifiers += item.getValue("vendorItemIdentifier").toString()
            }

            // find corresponding items
            val fields = BasicDBObject().apply {
                put("ownerId", body.getValue("ownerId"))
                put("userHashId", body.getValue("userHashId"))
                put("itemId", BasicDBObject("\$in", vendorItemIdentifiers))
            }

            val recommendedItems = collection.find(fields)
            recommendedItems.forEach {
                val diffBetweenLastTouch = currTimestamp - it["timestamp"].toString().toDouble()
                if (diffBetweenLastTouch > daysInMiliseconds && diffBetweenLastTouch < (daysInMiliseconds + resetPeriodInMiliseconds)) {
                    toRemoveIds += it.getString("itemId")
                } else if (diffBetweenLastTouch >= (daysInMiliseconds + resetPeriodInMiliseconds)) {
                    val queryFields = BasicDBObject().apply {
                        put("ownerId", body.getValue("ownerId"))
                        put("userHashId", body.getValue("userHashId"))
                        put("itemId", it["itemId"].toString())
                    }

                    val updatedFields = BasicDBObject("\$set", BasicDBObject("timestamp", currTimestamp))
                    collection.findOneAndUpdate(queryFields, updatedFields)
                }
            }

            requestedRecommendations = requestedRecommendations.filter {
                toRemoveIds.indexOf(it.getString("vendorItemIdentifier")) < 0
            }.toMutableList()

            body.put("recommendations", requestedRecommendations)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            mongoClient?.close()
        }

        return body
    }
}