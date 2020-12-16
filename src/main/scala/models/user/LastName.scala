package models.user

final case class LastName (lastName: String)
object LastName {
    def fromString(raw: String): LastName = {
        LastName(raw)
    }
    def toString(lastName: LastName): String = {
        lastName.lastName
    }
}
