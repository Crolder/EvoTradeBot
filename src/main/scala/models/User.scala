package models

import models.base.UserId
import models.user._

case class User(id: UserId,
                firstname: String,
                lastname: String,
                username: String,
                phoneNumber: Option[PhoneNumber]
               )
