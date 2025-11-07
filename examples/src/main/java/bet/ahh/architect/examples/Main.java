package bet.ahh.architect.examples;

public class Main {
	public static void main(String[] args) {
		System.out.println("=== User Examples ===");

		User user1 = UserBuilder.builder()
				.name(null)
				.age(30)
				.role("ADMIN")
				.nickname("Ali")
				.email("alice@example.com")
				.build();

		System.out.println(user1);

		User user2 = UserBuilder.builder().name("string").age(10).build();

		System.out.println(user2);

		User user3 = UserBuilder.builder()
				.name("Charlie")
				.age(35)
				.nickname("Chuck")
				.email("charlie@example.com")
				.phone("+1234567890")
				.build();

		System.out.println(user3);

		System.out.println("\n=== Product Examples ===");

		Product product1 = ProductBuilder.builder()
				.id("SKU-001")
				.name("Laptop")
				.price(999.99)
				.category("Electronics")
				.description("High-performance laptop")
				.build();

		System.out.println(product1);

		Product product2 = ProductBuilder.builder()
				.id("SKU-002")
				.name("Mouse")
				.price(29.99)
				.build();

		System.out.println(product2);

		Product product3 = ProductBuilder.builder()
				.id("SKU-003")
				.name("Keyboard")
				.price(79.99)
				.active(false)
				.build();

		System.out.println(product3);

		System.out.println("\n=== Email (Record) Examples ===");

		Email email1 = EmailBuilder.builder()
				.to("user@example.com")
				.subject("Welcome!")
				.priority("HIGH")
				.body("Thanks for signing up")
				.build();

		System.out.println(email1);

		Email email2 = EmailBuilder.builder()
				.to("admin@example.com")
				.subject("System Alert")
				.replyTo("noreply@example.com")
				.body("Server restarted")
				.cc("ops@example.com")
				.build();

		System.out.println(email2);

		Email email3 = EmailBuilder.builder()
				.to("info@example.com")
				.subject("Quick note")
				.build();

		System.out.println(email3);
	}
}
