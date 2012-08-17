package models

import com.mongodb._
import com.mongodb.casbah.Imports._
import com.osinka.subset._

object db {
  lazy val db = new Mongo("localhost") getDB "subset-test"
  lazy val tweets = db getCollection "tweets"
  lazy val users = db getCollection "users"
}

import models.db._

case class User(_id: ObjectId = new ObjectId, name: String)

object User {
  val name = "name".fieldOf[String]

  implicit object userReader extends ValueReader[User] {
    def unpack(o: Any): Option[User] =
      o match {
        case id: ObjectId =>
          Option(users findOne id) collect { case name(n) => new User(id, n) }
        case _ =>
          None
      }
  }

  // TODO This doesn't work yet; why is it Any instead of DBObject?
  /*
  implicit object userWriter extends ValueWriter[User] {
    val id = "_id".fieldOf[ObjectId]

    def pack(user: User): Option[Any] = {
      Some((id -> user._id) ~ (name -> user.name))
    }
  }
  */

  def findOneById(userId: ObjectId): Option[User] = {
    userReader.unpack(userId)
  }

  def findUserByName(userName: String): Option[User] = {
    import collection.JavaConverters._
    users.find(name === userName).iterator.asScala.collectFirst {
      case Document.DocumentId(id) ~ name(name) => User(id, name)
    }
  }
}

case class Tweet(content: String, user: User)

object Tweet {
  import User._
  val content = "content".fieldOf[String]
  val user = "user".fieldOf[User]

  def all: Seq[Tweet] = {
    import collection.JavaConverters._
    tweets.find.iterator.asScala map {
      case Document.DocumentId(id) ~ content(content) ~ user(user) => Tweet(content, user)
    } toSeq
  }

  def tweetsForUser(aUser: User): Seq[Tweet] = {
    import collection.JavaConverters._
    tweets.find(user.as[ObjectId] === aUser._id).iterator.asScala collect {
      case Document.DocumentId(id) ~ content(content) ~ user(user) => Tweet(content, user)
    } toSeq
  }
}
