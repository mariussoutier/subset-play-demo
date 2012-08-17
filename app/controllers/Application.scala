package controllers

import play.api._
import play.api.mvc._

import models._
import models.db._

import com.mongodb.casbah.Imports._

object Application extends Controller {

  def index = Action {
    // In combination with Casbah
    //MongoCollection(tweets).find

    Ok(views.html.index(Tweet.all))
  }

  def showTweetsForUser(id: ObjectId) = Action {
    User.findOneById(id) map { aUser =>
      val tweetContents = tweets.find(Tweet.user.as[ObjectId] === aUser._id) collect {
        case Tweet.content(content) => content
      }
      Ok(views.html.userTweets(aUser, tweetContents.toSeq))
    } getOrElse NotFound
  }

}
