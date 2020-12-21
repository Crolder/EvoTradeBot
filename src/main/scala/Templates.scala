package main.scala

import canoe.api.{Scenario, chatApi}
import canoe.models.{Chat, Contact, DetailedChat}
import canoe.models.InputFile.Existing
import canoe.models.messages.ContactMessage
import canoe.models.outgoing.PhotoContent
import models.apartment._
import models.base._
import models.car._
import models.computer._
import models.User
import models.Product

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

    def userTemplate(detailedChat: DetailedChat, user: User, products: List[Product]): String = {
        val phoneNumber = user.phoneNumber match {
            case Some(number) => number.phoneNumber
            case None => "no phone number"
        }
        s"""
           |My personal information:
           |
           |ID: ${detailedChat.id}
           |Name: ${detailedChat.firstName.getOrElse("No information")}
           |Surname: ${detailedChat.lastName.getOrElse("No information")}
           |Username: ${detailedChat.username.getOrElse("No information")}
           |Phone number : $phoneNumber
           |
           |You have ${products.size} products
           |""".stripMargin
    }

    def contactTemplate(user: User, chat: Chat): Contact = {
        ContactMessage(
            user.id.id.toInt,
            chat,
            java.time.LocalDate.now.getDayOfYear,
            Contact(
                user.phoneNumber.getOrElse("no phone number").toString,
                user.firstname,
                Some(user.lastname),
                Some(user.id.id.toInt),
                None)
        ).contact
    }
}