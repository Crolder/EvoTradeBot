package models.base

final case class UserId private (id: Long) extends AnyVal
object UserId {
    def fromLong(raw: Long): UserId = {
        UserId(raw)
    }
    def toLong(userId: UserId): Long = {
        userId.id
    }
}

