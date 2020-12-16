package models.base

import java.util.UUID

final case class ImageKey(imageKey: String) extends AnyVal
object ImageKey {
    def fromString(raw: String): ImageKey = {
        ImageKey(raw)
    }
    def toString(imageKey: ImageKey): String = {
        imageKey.imageKey
    }
}
