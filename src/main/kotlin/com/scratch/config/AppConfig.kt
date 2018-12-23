package com.scratch.config

class AppConfig {
    companion object {
        val NO_USER_ACT_ON_DAYS: Long = 30
        val NO_USER_ACT_ON_PERIOD: Long = 30
        val MONGO_CONNECTION_STRING: String = "mongodb://rfuser:rfpassword1@ds033037.mlab.com:33037/recommend_filter"
        val MONGO_DB_NAME: String = "recommend_filter"
        val MONGO_COLLECTION_NAME: String = "recommended_action"
        val APP_PORT: Int = 8080
    }
}