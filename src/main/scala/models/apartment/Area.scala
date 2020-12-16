package models.apartment

sealed abstract case class Area private (area: Int)
object Area {
    def of(area: String): Option[Area] = {
        area.toIntOption match {
            case Some(area) if area > 0 => Some(new Area(area) {})
            case _ => None
        }
    }

    def fromInt(raw: Int): Area = {
        Area.of(raw.toString).get
    }

    def toInt(area: Area): Int = {
        area.area
    }
}
