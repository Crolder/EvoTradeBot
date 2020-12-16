package models.user

final case class FirstName (firstName: String)
object FirstName {
    def fromString(raw: String): FirstName = {
        FirstName(raw)
    }
    def toString(firstName: FirstName): String = {
        firstName.firstName
    }
}
