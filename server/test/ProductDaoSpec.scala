import dao.ProductDao
import models.Product
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application

class ProductDaoSpec extends PlaySpec with ScalaFutures with GuiceOneAppPerSuite {
    "ProductDao" must(
        "Have default rows on database creation" in {
            val app2dao = Application.instanceCache[ProductDao]
            val dao: ProductDao = app2dao(app)

            val expected = Set(
                Product("PEPPER", "ALD2", "PEPPER is a robot moving with wheels and with" +
                    "a screen as human interaction", 7000.0),
                Product("NAO", "ALD1", "NAO is an humanoid robot.", 3500.0),
                Product("BEOBOT", "BEO1", "Beobot is a multipurpose robot.", 159.0)
            )

            dao.all().futureValue should contain theSameElementsAs (expected)
        }
    )
}
