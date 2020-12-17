package models.base

import org.scalacheck.Gen._
import org.scalatest.{EitherValues, OptionValues}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers._
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

class ImageKeySpec extends AnyFlatSpec with OptionValues with EitherValues with ScalaCheckDrivenPropertyChecks  {
    "ImageKey" should "convert ImageKey object to string" in {
        forAll(alphaNumStr) { v: String =>
            ImageKey.toString(ImageKey(v)) shouldBe v
        }
    }

    it should "convert string to ImageKey object" in {
        forAll(alphaNumStr) { v: String =>
            ImageKey.fromString(v) shouldBe ImageKey(v)
        }
    }
}
