package models.computer

sealed trait Cpu
object Cpu {
    final case object Amd extends Cpu
    final case object Intel extends Cpu
    final case object Unknown extends Cpu

    def fromString(raw: String): Cpu = {
        raw match {
            case "Amd" => Amd
            case "Intel" => Intel
            case _ => Unknown
        }
    }

    def toString(cpu: Cpu): String = {
        cpu match {
            case Amd => "Amd"
            case Intel => "Intel"
            case Unknown => "Unknown"
        }
    }
}
