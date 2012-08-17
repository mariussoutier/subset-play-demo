package models

import com.mongodb._
import com.mongodb.casbah.Imports._
import com.osinka.subset._

object db {
  lazy val db = new Mongo("localhost") getDB "subset-test"
  // Casbah makes it even more fluid
  lazy val tweets = new MongoCollection(db getCollection "tweets")
  lazy val users = new MongoCollection(db getCollection "users")
}

import models.db._

case class User(_id: ObjectId = new ObjectId, name: String)

object User {
  val name = "name".fieldOf[String]

  implicit object userReader extends ValueReader[User] {
    def unpack(o: Any): Option[User] =
      o match {
        case id: ObjectId =>
          users findOneByID id collect { case name(n) => new User(id, n) }
        //case dbo: DBObject => User(id, name)
        case _ => None
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
    users find(name === userName) collectFirst {
      case Document.DocumentId(id) ~ name(name) => User(id, name)
    }
  }
}

// TODO: at: java.util.Date
case class Tweet(content: String, user: User)

object Tweet {
  import User._
  val content = "content".fieldOf[String]
  val user = "user".fieldOf[User]

  def all: Seq[Tweet] = {
    tweets.find map {
      case Document.DocumentId(id) ~ content(content) ~ user(user) => Tweet(content, user)
    } toSeq
  }

  def tweetsForUser(aUser: User): Seq[Tweet] = {
    tweets find(user.as[ObjectId] === aUser._id) collect {
      case Document.DocumentId(id) ~ content(content) ~ user(user) => Tweet(content, user)
    } toSeq
  }
}
