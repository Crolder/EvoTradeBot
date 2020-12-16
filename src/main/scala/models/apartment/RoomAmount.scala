package models.apartment

sealed abstract case class RoomAmount private (roomAmount: Int)
object RoomAmount {
    def of(roomAmount: String): Option[RoomAmount] = {
        roomAmount.toIntOption match {
            case Some(roomAmount) if roomAmount > 0 => Some(new RoomAmount(roomAmount) {})
            case _ => None
        }
    }

    def fromInt(raw: Int): RoomAmount = {
        RoomAmount.of(raw.toString).get
    }

    def toInt(roomAmount: RoomAmount): Int = {
        roomAmount.roomAmount
    }
}
