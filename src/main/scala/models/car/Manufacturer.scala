package models.car

sealed trait Manufacturer
object Manufacturer {
    final case object Volvo extends Manufacturer
    final case object Bmw extends Manufacturer
    final case object Audi extends Manufacturer
    final case object Unknown extends Manufacturer

    def fromString(raw: String): Manufacturer = {
        raw match {
            case "Audi" => Audi
            case "Bmw" => Bmw
            case "Volvo" => Volvo
            case _ => Unknown
        }
    }

    def toString(manufacturer: Manufacturer): String = {
        manufacturer match {
            case Audi => "Audi"
            case Bmw => "Bmw"
            case Volvo => "Volvo"
            case Unknown => "Unknown"
        }
    }
}
