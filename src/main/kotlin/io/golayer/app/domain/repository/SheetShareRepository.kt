package io.golayer.app.domain.repository

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.golayer.app.config.StorageConfig.Companion.SPREADSHEET_SHARE_INDEX
import io.golayer.app.domain.CreatedSharedRecord
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.common.xcontent.XContentType
import org.elasticsearch.index.query.QueryBuilder
import org.elasticsearch.index.query.QueryBuilders.matchAllQuery
import org.elasticsearch.index.query.QueryBuilders.matchPhraseQuery
import org.elasticsearch.search.builder.SearchSourceBuilder


/*
 * Spreadsheet Sharing Repository operations for the elastic search
 */
class SheetShareRepository(private val client: RestHighLevelClient, private val mapper: ObjectMapper) {

    fun persist(content: String, index: IndexRequest = IndexRequest(SPREADSHEET_SHARE_INDEX)): String =
            requireNotNull(client.index(index.source(content, XContentType.JSON), RequestOptions.DEFAULT).id)

    /*
     * findAll restricts the result amount according elastic search restrictions for sync http commands
     * If required traverse a large data-set, consider scroll api or some optimized strategy like a event subscription
     * using a match all ES query
     */
    fun findAll(): Result<List<CreatedSharedRecord>> =
            runCatching {
                client.search(within(matchAllQuery()), RequestOptions.DEFAULT).hits
                        .mapNotNull { mapper.readValue<CreatedSharedRecord>(it.sourceAsString) }
            }

    /*
     * findAll restricts the result amount according elastic search restrictions for sync http commands
     * If required traverse a large data-set, consider scroll api or some optimized strategy like a event subscription
     * using a match phrase ES query
     */
    fun findByEmail(email: String): Result<List<CreatedSharedRecord>> =
            runCatching {
                client.search(within(matchPhraseQuery("email", email)), RequestOptions.DEFAULT).hits
                        .map { mapper.readValue<CreatedSharedRecord>(it.sourceAsString) }
            }


    /*
     * TODO the spreadsheet identifiers were planned to be in a index/table, previous function its memorized with this intention
     */
    fun isSpreadsheetValid(spreadsheetIdentifier: String): Boolean =
            listOf("HRReport", "Actuals", "Assumptions", "Dashboard").map { it.toUpperCase() }
                    .contains(spreadsheetIdentifier.toUpperCase())


    /*
     * Wraps ES SearchRequest with a query builder receiver
     */
    private fun within(query: QueryBuilder, index: String = SPREADSHEET_SHARE_INDEX) =
            with(SearchRequest(index)) {
                source(SearchSourceBuilder().also {
                    it.query(query)
                })
            }
}

