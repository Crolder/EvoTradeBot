package models.car

sealed abstract case class Mileage private (mileage: Int)
object Mileage {
    def of(mileage: String): Option[Mileage] = {
        mileage.toIntOption match {
            case Some(mileage) if mileage >= 0 & mileage <= 10000000 => Some(new Mileage(mileage) {})
            case _ => None
        }
    }

    def fromInt(raw: Int): Mileage = {
        Mileage.of(raw.toString).get
    }

    def toInt(mileage: Mileage): Int = {
        mileage.mileage
    }
}
