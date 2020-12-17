package models.base

sealed abstract case class Price private (price: Double)
object Price {
    def of(price: String): Option[Price] = {
        price.toDoubleOption match {
            case Some(price) if price >= 0 => Some(new Price(price) {})
            case _ => None
        }
    }

    def fromDouble(raw: Double): Price = {
        Price.of(raw.toString).get
    }
    def toDouble(price: Price): Double = {
        price.price
    }
}
