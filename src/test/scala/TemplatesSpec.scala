package main.scala

import canoe.models.InputFile.Existing
import canoe.models.outgoing.PhotoContent
import org.scalacheck.Gen._
import org.scalatest.{EitherValues, OptionValues}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers._
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import Templates._
import models.apartment._
import models.apartment.District._
import models.base._
import models.car.Manufacturer._
import models.car._
import models.computer.Cpu._
import models.computer._

class TemplatesSpec extends AnyFlatSpec with OptionValues with EitherValues with ScalaCheckDrivenPropertyChecks {
    "carTemplate" should "provide photocontent with correct content inside" in {
        val testImageKey = "AgACAgQAAxkBAAINol_aVeAZNxDcfindIqkd7jXnYq9pAAJytjEbbDjQUkuazneyxy0dJEUHKV0AAwEAAwIAA20AA7rJAQABHgQ"
        val testCaption =
            """
              |Manufacturer: Bmw
              |Year: 2001
              |Mileage: 424242 km
              |
              |Description: Test description
              |
              |Price: 123.0 €
              |""".stripMargin
        val manualPhotoContent = PhotoContent(Existing(testImageKey), testCaption)

        val functionPhotoContent = carTemplate(
            Bmw,
            Year.of(2001.toString).get,
            Mileage.of(424242.toString).get,
            Description("Test description"),
            Price.of(123.00.toString).get,
            ImageKey(testImageKey)
        )

        manualPhotoContent shouldBe functionPhotoContent
    }

    "apartmentTemplate" should "provide photocontent with correct content inside" in {
        val testImageKey = "AgACAgQAAxkBAAINol_aVeAZNxDcfindIqkd7jXnYq9pAAJytjEbbDjQUkuazneyxy0dJEUHKV0AAwEAAwIAA20AA7rJAQABHgQ"
        val testCaption =
            """
              |District: Imanta
              |Area: 42 m^2
              |Amount of rooms: 2
              |Floor: 7
              |
              |Description: Test description
              |
              |Price: 123.0 €
              |""".stripMargin
        val manualPhotoContent = PhotoContent(Existing(testImageKey), testCaption)

        val functionPhotoContent = apartmentTemplate(
            Imanta,
            Area.of(42.toString).get,
            RoomAmount.of(2.toString).get,
            Floor.of(7.toString).get,
            Description("Test description"),
            Price.of(123.00.toString).get,
            ImageKey(testImageKey)
        )

        manualPhotoContent shouldBe functionPhotoContent
    }

    "computerTemplate" should "provide photocontent with correct content inside" in {
        val testImageKey = "AgACAgQAAxkBAAINol_aVeAZNxDcfindIqkd7jXnYq9pAAJytjEbbDjQUkuazneyxy0dJEUHKV0AAwEAAwIAA20AA7rJAQABHgQ"
        val testCaption =
            """
              |CPU: Amd
              |RAM: 16 gb
              |Video card: GTX 2080
              |Storage: 520 gb
              |
              |Description: Test description
              |
              |Price: 654.0 €
              |""".stripMargin
        val manualPhotoContent = PhotoContent(Existing(testImageKey), testCaption)

        val functionPhotoContent = computerTemplate(
            Amd,
            Ram.of(16.toString).get,
            VideoCard("GTX 2080"),
            Storage.of(520.toString).get,
            Description("Test description"),
            Price.of(654.00.toString).get,
            ImageKey(testImageKey)
        )

        manualPhotoContent shouldBe functionPhotoContent
    }
}
