package models.computer

final case class VideoCard(videoCard: String)
object VideoCard {
    def fromString(raw: String): VideoCard = {
        VideoCard(raw)
    }
    def toString(videoCard: VideoCard): String = {
        videoCard.videoCard
    }
}
