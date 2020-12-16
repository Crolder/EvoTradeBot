package models.base

import java.util.UUID

final case class ProductId(id: UUID)
object ProductId {
    def fromString(raw: String): ProductId = {
        ProductId(UUID.fromString(raw))
    }
    def toString(productId: ProductId): String = {
        productId.id.toString
    }
}
