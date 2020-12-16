package models.car

sealed abstract case class Year private (year: Int)
object Year {
    def of(year: String): Option[Year] = {
        year.toIntOption match {
            case Some(year) if year >= 1900 & year <= 2021 => Some(new Year(year) {})
            case _ => None
        }
    }

    def fromInt(raw: Int): Year = {
        Year.of(raw.toString).get
    }

    def toInt(year: Year): Int = {
        year.year
    }
}
