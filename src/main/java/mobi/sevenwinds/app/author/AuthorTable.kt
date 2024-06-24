package mobi.sevenwinds.app.author

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable

object AuthorTable : IntIdTable("author") {
    val fullName = varchar("fullname", 255)
    val date = datetime("date_of_creation")
}

class AuthorEntity(id: EntityID<Int>) : IntEntity(id) {

    companion object : IntEntityClass<AuthorEntity>(AuthorTable)

    var fullName by AuthorTable.fullName
    var date by AuthorTable.date

    fun toResponse(): AuthorRecord {
        return AuthorRecord(fullName)
    }
}

