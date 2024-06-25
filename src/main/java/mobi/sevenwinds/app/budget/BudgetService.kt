package mobi.sevenwinds.app.budget

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mobi.sevenwinds.app.author.AuthorTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.format.DateTimeFormatter

object BudgetService {
    suspend fun addRecord(body: BudgetRecord): BudgetRecord = withContext(Dispatchers.IO) {
        transaction {
            val entity = BudgetEntity.new {
                this.year = body.year
                this.month = body.month
                this.amount = body.amount
                this.type = body.type
                this.authorId = body.authorId
            }

            return@transaction entity.toResponse()
        }
    }

    suspend fun getYearStats(param: BudgetYearParam): BudgetYearStatsResponse = withContext(Dispatchers.IO) {
        transaction {
            val total = BudgetTable.select { BudgetTable.year eq param.year }.count()

            val baseQuery = BudgetTable
                .join(AuthorTable, JoinType.LEFT, additionalConstraint = { BudgetTable.authorId eq AuthorTable.id })
                .slice(BudgetTable.columns + AuthorTable.fullName + AuthorTable.date)
                .select { BudgetTable.year eq param.year }

            val filteredQuery = if (param.authorName != null) {
                baseQuery.andWhere { AuthorTable.fullName.lowerCase() like "%${param.authorName.lowercase()}%" }
            } else {
                baseQuery
            }

            val query = filteredQuery
                .orderBy(BudgetTable.month to SortOrder.ASC, BudgetTable.amount to SortOrder.DESC)
                .limit(param.limit, param.offset)


            val data = query.map { row ->
                val fullName = row[AuthorTable.fullName]?: "Unknown"
                val date = row[AuthorTable.date]?.toString("yyyy-MM-dd HH:mm:ss")?: "N/A"
                BudgetEntity.wrapRow(row).toResponse(fullName, date)
            }

            val allRecordsQuery = BudgetTable.select { BudgetTable.year eq param.year }
            val allRecords = BudgetEntity.wrapRows(allRecordsQuery).map { it.toResponse() }
            val sumByType = allRecords.groupBy { it.type.name }.mapValues { it.value.sumOf { v -> v.amount } }

            return@transaction BudgetYearStatsResponse(
                total = total,
                totalByType = sumByType,
                items = data
            )
        }
    }
}
