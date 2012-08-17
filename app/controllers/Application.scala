package controllers

import play.api._
import play.api.mvc._

import models._
import models.db._

import com.mongodb.casbah.Imports._

object Application extends Controller {

  def index = Action {
    import collection.JavaConverters._
    Ok(views.html.index(Tweet.all))
  }

  def showTweetsForUser(id: ObjectId) = Action {
    import collection.JavaConverters._
    User.findOneById(id) map { aUser =>

      val tweetContents: Seq[String] = tweets.find(Tweet.user.as[ObjectId] === aUser._id).iterator.asScala collect {
        case Tweet.content(content) => content
      } toSeq

      Ok(views.html.userTweets(aUser, tweetContents))
    } getOrElse NotFound
  }

}
