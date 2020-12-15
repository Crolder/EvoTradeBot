package services

import canoe.api.models.Keyboard
import canoe.models.{KeyboardButton, ReplyKeyboardMarkup}

object KeyboardService {
    def createKeyboard(column: Boolean, buttons: String*): Keyboard.Reply = {
        val keyboardButtons = buttons.map(button => KeyboardButton.text(button))
        if (column) {
            val inlineKeyboardMarkUp: ReplyKeyboardMarkup = ReplyKeyboardMarkup.singleColumn(keyboardButtons)
            Keyboard.Reply(inlineKeyboardMarkUp)
        } else {
            val inlineKeyboardMarkUp: ReplyKeyboardMarkup = ReplyKeyboardMarkup.singleRow(keyboardButtons)
            Keyboard.Reply(inlineKeyboardMarkUp)
        }
    }
}
