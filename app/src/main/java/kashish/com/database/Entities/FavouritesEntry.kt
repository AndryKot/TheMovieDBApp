package kashish.com.database.Entities

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey
import android.arch.persistence.room.TypeConverters
import kashish.com.utils.Constants.Companion.FAVOURITES
import java.util.*

/**
 * Created by Kashish on 11-08-2018.
 */

@Entity(tableName = "favourites")
class FavouritesEntry {

    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
    var movieId: Int? = null
    var voteCount: Int? = null
    var video: Boolean? = null
    var voteAverage: Float? = null
    var title: String? = null
    var popularity: Float? = null
    var posterPath: String? = null
    var originalLanguage: String? = null
    var originalTitle: String? = null
    var genreIds: String? = null
    var backdropPath: String? = null
    var adult: Boolean? = null
    var overview: String? = null
    var releaseDate: String? = null
    var contentType: Int? = null
    var totalPages: Int? = null
    var genreString: String? = ""
    var timeAdded: Date? = null
    var tableName: Int = FAVOURITES
}