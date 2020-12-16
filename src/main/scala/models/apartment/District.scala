package models.apartment

sealed trait District
object District {
    final case object Imanta extends District
    final case object Center extends District
    final case object Purvciems extends District
    final case object Unknown extends District

    def fromString(raw: String): District = {
        raw match {
            case "Imanta" => Imanta
            case "Center" => Center
            case "Purvciems" => Purvciems
            case _ => Unknown
        }
    }

    def toString(district: District): String = {
        district match {
            case Imanta => "Imanta"
            case Center => "Center"
            case Purvciems => "Purvciems"
            case Unknown => "Unknown"
        }
    }
}
