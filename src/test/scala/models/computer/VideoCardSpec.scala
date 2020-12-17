package models.computer

import org.scalacheck.Gen._
import org.scalatest.{EitherValues, OptionValues}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers._
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

class VideoCardSpec extends AnyFlatSpec with OptionValues with EitherValues with ScalaCheckDrivenPropertyChecks  {
    "VideoCard" should "convert VideoCard object to string" in {
        forAll(alphaNumStr) { v: String =>
            VideoCard.toString(VideoCard(v)) shouldBe v
        }
    }

    it should "convert string to VideoCard object" in {
        forAll(alphaNumStr) { v: String =>
            VideoCard.fromString(v) shouldBe VideoCard(v)
        }
    }
}
