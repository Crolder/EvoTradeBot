package models.base

case class Description(description: String)
object Description {
    def fromString(raw: String): Description = {
        Description(raw)
    }
    def toString(description: Description): String = {
        description.description
    }
}