package models.computer

sealed abstract case class Ram private (ram: Int)
object Ram {
    def of(ram: String): Option[Ram] = {
        ram.toIntOption match {
            case Some(ram) if ram > 0 => Some(new Ram(ram) {})
            case _ => None
        }
    }

    def fromInt(raw: Int): Ram = {
        Ram.of(raw.toString).get
    }
    def toInt(ram: Ram): Int = {
        ram.ram
    }
}
