package models

import models.apartment._
import models.base._
import models.car._
import models.computer._


sealed trait Product{
    val id: ProductId
    val userId: UserId
    val price: Price
    val descripion: Description
    val imageKey: ImageKey
}

object Product {
    case class Car(
                    id: ProductId,
                    userId: UserId,
                    price: Price,
                    manufacturer: Manufacturer,
                    year: Year,
                    mileage: Mileage,
                    descripion: Description,
                    imageKey: ImageKey
                  ) extends Product

    case class Apartment(
                    id: ProductId,
                    userId: UserId,
                    price: Price,
                    district: District,
                    area: Area,
                    roomAmount: RoomAmount,
                    floor: Floor,
                    descripion: Description,
                    imageKey: ImageKey
                  ) extends Product

    case class Computer(
                          id: ProductId,
                          userId: UserId,
                          price: Price,
                          cpu: Cpu,
                          ram: Ram,
                          videoCard: VideoCard,
                          storage: Storage,
                          descripion: Description,
                          imageKey: ImageKey
                        ) extends Product
}



