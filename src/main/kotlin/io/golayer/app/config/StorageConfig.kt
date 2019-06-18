package io.golayer.app.config

import org.apache.http.HttpHost
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient

/*
 * defines Elastic-search as storage configuration
 * Using the official ES provided RestHighLevelClient java API
 */
class StorageConfig(vararg hosts: Pair<String, Int>) {
    private val client = RestHighLevelClient(
            RestClient.builder(*hosts.map { HttpHost(it.first, it.second, "http") }.toTypedArray())
    )

    fun getClient(): RestHighLevelClient = client

    companion object {
        const val SPREADSHEET_SHARE_INDEX = "sheet_share"
        const val COMMANDS_INDEX = "produced_commands"
    }
}
