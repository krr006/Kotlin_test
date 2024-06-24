package mobi.sevenwinds.app.author

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime

object AuthorService {
    suspend fun addRecord(body: AuthorRecord): AuthorRecord = withContext(Dispatchers.IO){
        transaction {
            val entity = AuthorEntity.new {
                this.fullName = body.fullName
                this.date = DateTime.now()
            }

            return@transaction entity.toResponse()
        }
    }
}