package jkugiya.akka.extension.firebase

import akka.actor.{ ExtendedActorSystem, Extension, ExtensionId, ExtensionIdProvider }
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.{ FirebaseApp, FirebaseOptions }
import com.typesafe.config.Config

object Firebase extends ExtensionId[Firebase] with ExtensionIdProvider {

  override def createExtension(system: ExtendedActorSystem): Firebase = new FirebaseImpl(system)

  override def lookup(): ExtensionId[_ <: Extension] = Firebase

}

trait Firebase extends Extension {

  /**
   * @return default application
   */
  def app: FirebaseApp

  /**
   * @param name application's name
   * @return the application for name
   */
  def appForName(name: String): FirebaseApp

  /**
   * @return default database
   */
  def database: FirebaseDatabase

  /**
   * @param name application's name
   * @return the database for application
   */
  def databaseForAppName(name: String): FirebaseDatabase

  /**
   * @return default auth
   */
  def auth: FirebaseAuth

  /**
   * @param name application's name
   * @return the auth for application
   */
  def authForAppName(name: String): FirebaseAuth
}

class FirebaseImpl(system: ExtendedActorSystem) extends Firebase {

  private lazy val rootConfig = system.settings.config.getConfig("ext-firebase")

  private def createFirebaseOptions(config: Config): FirebaseOptions = {
    val builder = new FirebaseOptions.Builder()
      .setDatabaseUrl(config.getString("database-url"))
    if (config.hasPath("service-account")) {
      val in = getClass
        .getClassLoader
        .getResourceAsStream(config.getString("service-account"))
      builder.setServiceAccount(in)
    }
    if (config.hasPath("database-auth")) {
      import collection.JavaConverters._
      val auth = config.getConfig("database-auth")
      // this work only if config's values are number or string.
      val variables =
        auth.entrySet().asScala.map { entry =>
          val key = entry.getKey
          val value = entry.getValue
          key -> value.unwrapped()
        }.toMap.asJava
      builder.setDatabaseAuthVariableOverride(variables)
    }
    builder.build()
  }

  /**
   * @return default application
   */
  override val app = {
    val firebaseOptions = createFirebaseOptions(rootConfig)
    FirebaseApp.initializeApp(firebaseOptions)
  }

  if (rootConfig.hasPath("apps")) {
    import collection.JavaConverters._
    for (cf <- rootConfig.getConfigList("apps").asScala) {
      val options = createFirebaseOptions(cf)
      val appName = cf.getString("application-name")
      FirebaseApp.initializeApp(options, appName)
    }
  }

  /**
   * @param name application's name
   * @return the application for name
   */
  override def appForName(name: String): FirebaseApp = FirebaseApp.getInstance(name)

  /**
   * @return default database
   */
  override val database = FirebaseDatabase.getInstance(app)

  /**
   * @param name application's name
   * @return the database for application
   */
  override def databaseForAppName(name: String): FirebaseDatabase =
    FirebaseDatabase.getInstance(appForName(name))

  /**
   * @return default auth
   */
  override def auth: FirebaseAuth = FirebaseAuth.getInstance(app)

  /**
   * @param name application's name
   * @return the auth for application
   */
  override def authForAppName(name: String): FirebaseAuth =
    FirebaseAuth.getInstance(appForName(name))

}
