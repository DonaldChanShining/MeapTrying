package modules

import javax.inject.Inject
import com.google.inject.AbstractModule
import play.api.db.Database
import play.api.libs.Crypto
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import generated.Tables._


/**
  *
  * Created by donglai.chen on 2016/8/12.
  *
  */
class Fixtures @Inject()(val crypto: Crypto, val db: Database) extends DatabaseFixtures {

  db.withTransaction { connection =>
    val sql = DSL.using(connection, SQLDialect.POSTGRES_9_4)

    if (sql.fetchCount(USER) == 0) {
      val hashedPassword = crypto.sign("secret")
      sql.insertInto(USER).columns(USER.EMAIL,USER.FIRSTNAME,USER.LASTNAME,USER.PASSWORD)
          .values("test@gmail.com","Donald","Chen",hashedPassword).execute()
    }

  }
}

trait DatabaseFixtures

class FixturesModule extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[DatabaseFixtures
      ]).to(classOf[Fixtures]).asEagerSingleton()
  }
}
