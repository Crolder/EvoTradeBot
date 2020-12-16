package db

import doobie.Meta
import models.apartment._
import models.base._
import models.car._
import models.computer._
import models.user.PhoneNumber

object DbMappings {
    //Base mappings
    implicit val userIdMeta: Meta[UserId] = Meta[Long].timap(UserId.fromLong)(UserId.toLong)
    implicit val productIdMeta: Meta[ProductId] = Meta[String].timap(ProductId.fromString)(ProductId.toString)
    implicit val priceMeta: Meta[Price] = Meta[Double].timap(Price.fromDouble)(Price.toDouble)
    implicit val imageKeyMeta: Meta[ImageKey] = Meta[String].timap(ImageKey.fromString)(ImageKey.toString)
    implicit val descriptionMeta: Meta[Description] = Meta[String].timap(Description.fromString)(Description.toString)
    implicit val phoneNumberMeta: Meta[PhoneNumber] = Meta[String].timap(PhoneNumber.fromString)(PhoneNumber.toString)

    //Car mappings
    implicit val yearMeta: Meta[Year] = Meta[Int].timap(Year.fromInt)(Year.toInt)
    implicit val mileageMeta: Meta[Mileage] = Meta[Int].timap(Mileage.fromInt)(Mileage.toInt)
    implicit val manufacturerMeta: Meta[Manufacturer] = Meta[String].timap(Manufacturer.fromString)(Manufacturer.toString)

    //Apartment mappings
    implicit val districtMeta: Meta[District] = Meta[String].timap(District.fromString)(District.toString)
    implicit val areaMeta: Meta[Area] = Meta[Int].timap(Area.fromInt)(Area.toInt)
    implicit val floorMeta: Meta[Floor] = Meta[Int].timap(Floor.fromInt)(Floor.toInt)
    implicit val roomAmountMeta: Meta[RoomAmount] = Meta[Int].timap(RoomAmount.fromInt)(RoomAmount.toInt)

    //Computer mappings
    implicit val cpuMeta: Meta[Cpu] = Meta[String].timap(Cpu.fromString)(Cpu.toString)
    implicit val ramMeta: Meta[Ram] = Meta[Int].timap(Ram.fromInt)(Ram.toInt)
    implicit val storageMeta: Meta[Storage] = Meta[Int].timap(Storage.fromInt)(Storage.toInt)
    implicit val videoCardMeta: Meta[VideoCard] = Meta[String].timap(VideoCard.fromString)(VideoCard.toString)
}
