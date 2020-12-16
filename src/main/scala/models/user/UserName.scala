package models.user

final case class UserName (userName: String)
object UserName {
    def fromString(raw: String): UserName = {
        UserName(raw)
    }
    def toString(userName: UserName): String = {
        userName.userName
    }
}
