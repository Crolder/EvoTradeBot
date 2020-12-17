package models.computer

sealed abstract case class Storage private (storage: Int)
object Storage {
    def of(storage: String): Option[Storage] = {
        storage.toIntOption match {
            case Some(storage) if storage > 0 => Some(new Storage(storage) {})
            case _ => None
        }
    }

    def fromInt(raw: Int): Storage = {
        Storage.of(raw.toString).get
    }
    def toInt(storage: Storage): Int = {
        storage.storage
    }
}
