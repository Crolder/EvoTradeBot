package main.scala

import canoe.models.InputFile.Existing
import canoe.models.outgoing.PhotoContent
import models.apartment._
import models.base._
import models.car._
import models.computer._

object Templates {


    def carTemplate(
                     manufacturer: Manufacturer,
                     year: Year,
                     mileage: Mileage,
                     description: Description,
                     price: Price,
                     imageKey: ImageKey
                   ): PhotoContent = {
        PhotoContent(
            Existing(imageKey.imageKey),
            s"""
               |Manufacturer: ${manufacturer.toString}
               |Year: ${year.year}
               |Mileage: ${mileage.mileage} km
               |
               |Description: ${description.description}
               |
               |Price: ${price.price} €
               |""".stripMargin
        )
    }

    def apartmentTemplate(
                           district: District,
                           area: Area,
                           roomAmount: RoomAmount,
                           floor: Floor,
                           description: Description,
                           price: Price,
                           imageKey: ImageKey
                         ): PhotoContent = {
        PhotoContent(
            Existing(imageKey.imageKey),
            s"""
               |District: ${district.toString}
               |Area: ${area.area} m^2
               |Amount of rooms: ${roomAmount.roomAmount}
               |Floor: ${floor.floor}
               |
               |Description: ${description.description}
               |
               |Price: ${price.price} €
               |""".stripMargin
        )
    }

    def computerTemplate(
                          cpu: Cpu,
                          ram: Ram,
                          videoCard: VideoCard,
                          storage: Storage,
                          description: Description,
                          price: Price,
                          imageKey: ImageKey
                         ): PhotoContent = {
        PhotoContent(
            Existing(imageKey.imageKey),
            s"""
               |CPU: ${cpu.toString}
               |RAM: ${ram.ram} gb
               |Video card: ${videoCard.videoCard}
               |Storage: ${storage.storage} gb
               |
               |Description: ${description.description}
               |
               |Price: ${price.price} €
               |""".stripMargin
        )
    }
}
