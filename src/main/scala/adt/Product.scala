package adt

sealed trait Product{
    val id: String
    val userId: Long
    val price: Int
    val imageKey: String
}

object Product {
    case class Car(
                    id: String,
                    userId: Long,
                    price: Int,
                    manufacturer: String,
                    year: Int,
                    mileage: Int,
                    imageKey: String
                  ) extends Product

    case class Apartment(
                    id: String,
                    userId: Long,
                    price: Int,
                    district: String,
                    area: Int,
                    roomAmount: Int,
                    floor: Int,
                    imageKey: String
                  ) extends Product

    case class Computer(
                          id: String,
                          userId: Long,
                          price: Int,
                          cpu: String,
                          ram: Int,
                          videoCard: String,
                          storage: Int,
                          imageKey: String
                        ) extends Product
}



