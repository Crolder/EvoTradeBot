package models.base

import org.scalacheck.Gen._
import org.scalatest.{EitherValues, OptionValues}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers._
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

class UserIdSpec extends AnyFlatSpec with OptionValues with EitherValues with ScalaCheckDrivenPropertyChecks  {
    "UserId" should "convert UserId object to long" in {
        forAll(posNum[Long]) { v: Long =>
            UserId.toLong(UserId(v)) shouldBe v
        }
    }

    it should "convert long to UserId object" in {
        forAll(posNum[Long]) { v: Long =>
            UserId.fromLong(v) shouldBe UserId(v)
        }
    }
}
