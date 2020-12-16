package models.user

sealed abstract case class PhoneNumber private(phoneNumber: String)
object PhoneNumber {
    private val PhoneNumberPattern = "^(\\+3712\\d{7})$".r

    def of(phoneNumber: String): Option[PhoneNumber] = {
        phoneNumber match {
            case PhoneNumberPattern(number) => Some( new PhoneNumber(number) {})
            case _ => None
        }
    }

    def fromString(raw: String): PhoneNumber = {
        PhoneNumber.of(raw).get
    }
    def toString(phoneNumber: PhoneNumber): String = {
        phoneNumber.phoneNumber
    }
}
