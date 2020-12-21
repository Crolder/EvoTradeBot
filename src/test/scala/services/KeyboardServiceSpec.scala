package services

import org.scalacheck.Gen._
import org.scalatest.{EitherValues, OptionValues}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers._
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

import canoe.api.models.Keyboard
import canoe.models.{KeyboardButton, ReplyKeyboardMarkup}

class KeyboardServiceSpec extends AnyFlatSpec with OptionValues with EitherValues with ScalaCheckDrivenPropertyChecks  {
    "createKeyboard" should "provide correct keyboard" in {
        val testBtn1 = KeyboardButton.text("testBtn1")
        val testBtn2 = KeyboardButton.text("testBtn2")
        val testBtn3 = KeyboardButton.text("testBtn3")
        val inlineKeyboardMarkUp: ReplyKeyboardMarkup = ReplyKeyboardMarkup.singleColumn(Seq(testBtn1,testBtn2,testBtn3))
        val keyboardManual = Keyboard.Reply(inlineKeyboardMarkUp)

        val keyboardFunc = KeyboardService.createKeyboard(true,"testBtn1", "testBtn2", "testBtn3")

        for ((manualSeq, functionSeq) <- keyboardManual.markup.keyboard.zip(keyboardFunc.markup.keyboard)) {
            for((manualButton,functionButton) <- manualSeq.zip(functionSeq)) {
                manualButton.text shouldBe functionButton.text
            }
        }
    }
}
