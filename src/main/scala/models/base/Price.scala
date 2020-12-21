package models.base

import scala.math.BigDecimal.RoundingMode

sealed abstract case class Price private (price: BigDecimal)
object Price {
    def of(price: String): Option[Price] = {
        price.toDoubleOption match {
            case Some(price) if price >= 0 => Some(new Price(BigDecimal(price).setScale(2, RoundingMode.HALF_UP)) {})
            case _ => None
        }
    }

    def fromDecimal(raw: BigDecimal): Price = {
        Price.of(raw.toString).get
    }
    def toDecimal(price: Price): BigDecimal = {
        price.price
    }
}
