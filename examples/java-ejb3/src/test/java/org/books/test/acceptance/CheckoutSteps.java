package org.books.test.acceptance;

import static org.junit.Assert.*;
 
import java.util.List;

import javax.ejb.EJB;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.openejb.api.LocalClient;
import org.books.business.CartManager;
import org.books.dao.BookDao;
import org.books.dao.OrderDao;
import org.books.domain.Book;
import org.books.domain.Order;

import cuke4duke.Given;
import cuke4duke.Then;
import cuke4duke.When;

@LocalClient
public class CheckoutSteps extends ContainerSteps {

	@EJB private BookDao bookDao;
	@EJB private OrderDao orderDao;
	@EJB private CartManager cartManager;

	public CheckoutSteps(ContainerInitializer initializer) throws NamingException {
		super(initializer);
	}

	@Given("^my shopping cart contains ([\\d]+) book with price (.+)$")
	public void cartContainsBook(int count, double price) throws Exception {
		for (int i = 0; i < count; i++) {
			Book book = new Book();
			book.setPrice(price);
			bookDao.addBook(book); // book must be persisted in order to persist the order later
			
			cartManager.AddBook(book, 1);
		}
	}

	@When("^I check out the cart$")
	public void checkoutCart() throws Exception {

		cartManager.checkout();
	}

	@Then("^an order should be created with total price (.+)$")
	public void checkOrderCreationAndPrice(double price) {

		List<Order> orders = orderDao.getOrders();
		assertEquals(1, orders.size());
		Order order = orders.get(0);
		assertEquals(price, order.getPrice(), 0);
	}
	
	@Then("^the order status should be '(.*)'$")
	public void theOrderStateShouldBe(String state) {
		Order.Status status = Order.Status.valueOf(state);
		
		List<Order> orders = orderDao.getOrders();
		assertEquals(1, orders.size());
		Order order = orders.get(0);
		assertEquals(status, order.getStatus());
	}
}
