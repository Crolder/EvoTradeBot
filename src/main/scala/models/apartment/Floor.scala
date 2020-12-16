package models.apartment

sealed abstract case class Floor private (floor: Int)
object Floor {
    def of(floor: String): Option[Floor] = {
        floor.toIntOption match {
            case Some(floor) if floor > 0 => Some(new Floor(floor) {})
            case _ => None
        }
    }

    def fromInt(raw: Int): Floor = {
        Floor.of(raw.toString).get
    }

    def toInt(floor: Floor): Int = {
        floor.floor
    }
}
